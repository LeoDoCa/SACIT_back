package mx.edu.utez.sacit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "requiered_documents")
public class RequiredDocuments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(updatable = false, nullable = false, unique = true)
    private UUID uuid;
    private String name;

    @ManyToOne
    @JoinColumn(name = "procedure_id", nullable = false)
    @JsonBackReference
    private Procedures procedure;

    @ManyToMany
    @JoinTable(name = "required_uploaded_documents", joinColumns = @JoinColumn(name = "required_document_id"), inverseJoinColumns = @JoinColumn(name = "uploaded_document_id"))
    @JsonIgnore
    private Set<UploadedDocuments> uploadedDocuments = new HashSet<>();

    @PrePersist
    public void generateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }
}
