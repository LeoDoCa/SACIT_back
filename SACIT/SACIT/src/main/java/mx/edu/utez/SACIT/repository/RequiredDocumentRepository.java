package mx.edu.utez.SACIT.repository;

import mx.edu.utez.SACIT.model.RequiredDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RequiredDocumentRepository extends JpaRepository<RequiredDocuments, Integer> {
    RequiredDocuments findByUuid(UUID uuid);
}
