package mx.edu.utez.sacit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "windows")
public class Window {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(updatable = false, nullable = false, unique = true)
    private UUID uuid;

    private String status;

    @Column(name = "window_number", unique = true, updatable = false)
    private Integer windowNumber;


    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;


    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonProperty(value = "attendant")
    @JsonIgnoreProperties({"password", "creationDate", "role", "createdProcedures", "attendedWindows", "appointments"})

    private UserModel attendant;


    @JsonIgnore
    @OneToMany(mappedBy = "window")
    private Set<AvailableSchedule> availableSchedules = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "window")
    private Set<Appointments> appointments = new HashSet<>();

    @PrePersist
    public void generateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    public Window(UUID uuid) {
        this.uuid = uuid;
    }



}
