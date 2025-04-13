package mx.edu.utez.SACIT.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "uploaded_documents")
public class UploadedDocuments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column( updatable = false, nullable = false,unique = true)
    private UUID uuid;

    @Column(name = "uploaded_date")
    private LocalDate uploadedDate;

    @Lob
    @Column(name = "document", columnDefinition = "LONGBLOB")
    private byte[] document;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    @JsonBackReference
    private Appointments appointment;

    @ManyToMany(mappedBy = "uploadedDocuments")
    @JsonIgnore
    private Set<RequiredDocuments> requiredDocuments = new HashSet<>();
}
