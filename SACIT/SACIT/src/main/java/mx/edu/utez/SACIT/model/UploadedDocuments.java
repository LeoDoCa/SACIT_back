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
@Table(name = "uploaded_documents")
public class UploadedDocuments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column( updatable = false, nullable = false,unique = true)
    private UUID uuid;

    @Column(name = "uploaded_date")
    private String uploadedDate;

    @Lob
    @Column(name = "document", columnDefinition = "LONGBLOB")
    private byte[] document;

    @ManyToOne
    @JoinColumn(name = "appointment_id")
    private Appointments appointment;

    @ManyToMany(mappedBy = "uploadedDocuments")
    private Set<RequiredDocuments> requiredDocuments = new HashSet<>();
}
