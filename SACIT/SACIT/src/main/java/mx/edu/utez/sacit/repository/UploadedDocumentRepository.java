package mx.edu.utez.sacit.repository;

import mx.edu.utez.sacit.model.Appointments;
import mx.edu.utez.sacit.model.UploadedDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UploadedDocumentRepository extends JpaRepository<UploadedDocuments, Integer> {
    UploadedDocuments findByUuid(UUID uuid);
    List<UploadedDocuments> findByAppointment(Appointments appointment);
}
