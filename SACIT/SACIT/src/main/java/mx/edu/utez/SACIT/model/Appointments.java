package mx.edu.utez.SACIT.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column( updatable = false, nullable = false,unique = true)
    private UUID uuid;

    private String phone;

    private LocalDate date;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @Column(name = "confirmation_code")
    private String confirmationCode;

    @Column(name = "cancellation_reason", length = 250)
    private String cancellationReason;

    private String status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel user;

    @ManyToOne
    @JoinColumn(name = "procedure_id",nullable = false)
    private Procedures procedure;


    @ManyToOne
    @JoinColumn(name = "window_id")
    private Window window;

    @OneToMany(mappedBy = "appointment")
    private Set<UploadedDocuments> uploadedDocuments = new HashSet<>();
}
