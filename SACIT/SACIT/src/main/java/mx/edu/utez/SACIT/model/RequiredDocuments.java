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
@Table(name = "requiered_documents")
public class RequiredDocuments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column( updatable = false, nullable = false,unique = true)
    private UUID uuid;

    private String name;
    private String description;
    private Boolean mandatory;

   @ManyToOne
   @JoinColumn(name = "procedure_id")
   private Procedures procedure;

   @ManyToMany
   @JoinTable(
       name = "required_uploaded_documents",
       joinColumns = @JoinColumn(name = "required_document_id"),
       inverseJoinColumns = @JoinColumn(name = "uploaded_document_id")
   )
   private Set<UploadedDocuments> uploadedDocuments = new HashSet<>();
}
