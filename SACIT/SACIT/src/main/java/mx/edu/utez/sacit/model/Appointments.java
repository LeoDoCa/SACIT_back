package mx.edu.utez.sacit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
    private LocalDate date;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    private String status;

    @JsonIgnore
    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id",nullable = true)
    @JsonBackReference
    private UserModel user;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "procedure_id",nullable = false)
    private Procedures procedure;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "window_id")
    private Window window;

    @JsonIgnore
    @OneToMany(mappedBy = "appointment")
    @JsonManagedReference
    private Set<UploadedDocuments> uploadedDocuments = new HashSet<>();

    @JsonIgnore
    public Set<RequiredDocuments> getRequiredDocumentsForProcedure() {
        if (this.procedure != null) {
            return this.procedure.getRequieredDocuments();
        }
        return new HashSet<>();
    }
    public boolean hasAllRequiredDocuments() {
        Set<RequiredDocuments> requiredDocs = getRequiredDocumentsForProcedure();

        for (RequiredDocuments req : requiredDocs) {
            boolean found = false;
            for (UploadedDocuments upload : this.uploadedDocuments) {
                if (upload.getRequiredDocuments().contains(req)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        return true;
    }
}
