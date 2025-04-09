package mx.edu.utez.SACIT.repository;

import mx.edu.utez.SACIT.model.RequieredDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RequieredDocumentRepository extends JpaRepository<RequieredDocuments, Integer> {
    RequieredDocuments findByUuid(UUID uuid);
}