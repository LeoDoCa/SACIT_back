package mx.edu.utez.SACIT.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @Column( updatable = false, nullable = false,unique = true)
    private UUID uuid;

    private String status;
    @Column(name = "window_number")
    private Integer windowNumber;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel attendant;

    @OneToMany(mappedBy = "window")
    private Set<WindowSchedule> schedules = new HashSet<>();

    @OneToMany(mappedBy = "window")
    private Set<AvailableSchedule> availableSchedules = new HashSet<>();

    @OneToMany(mappedBy = "window")
    private Set<Appointments> appointments = new HashSet<>();

}
