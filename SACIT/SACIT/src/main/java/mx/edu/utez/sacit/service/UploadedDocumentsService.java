package mx.edu.utez.sacit.service;

import mx.edu.utez.sacit.model.Appointments;
import mx.edu.utez.sacit.model.UploadedDocuments;
import mx.edu.utez.sacit.repository.AppointmentRepository;
import mx.edu.utez.sacit.repository.UploadedDocumentRepository;
import mx.edu.utez.sacit.utils.Utilities;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class UploadedDocumentsService {

    private final UploadedDocumentRepository uploadedDocumentRepository;

    private final AppointmentRepository appointmentRepository;

    public UploadedDocumentsService(UploadedDocumentRepository uploadedDocumentRepository, AppointmentRepository appointmentRepository) {
        this.uploadedDocumentRepository = uploadedDocumentRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getUploadedDocumentsForAppointment(UUID appointmentUuid) {
        try {
            Appointments appointment = appointmentRepository.findByUuid(appointmentUuid);
            if (appointment == null) {
                return Utilities.generateResponse(HttpStatus.NOT_FOUND, "Cita no encontrada", "404");
            }

            List<UploadedDocuments> uploadedDocs = uploadedDocumentRepository.findByAppointment(appointment);
            if (uploadedDocs.isEmpty()) {
                return Utilities.generateResponse(HttpStatus.NO_CONTENT, "No se encontraron documentos", "204");
            }

            List<Map<String, String>> encodedDocuments = new ArrayList<>();

            for (UploadedDocuments document : uploadedDocs) {
                byte[] documentBytes = document.getDocument();
                if (documentBytes == null || documentBytes.length == 0) {
                    continue;
                }

                String encodedContent = Base64.getEncoder().encodeToString(documentBytes);

                Map<String, String> documentResponse = new HashMap<>();
                documentResponse.put("fileName", document.getFileName());
                documentResponse.put("fileContent", encodedContent);

                encodedDocuments.add(documentResponse);
            }

            return ResponseEntity.ok(encodedDocuments);

        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al recuperar documentos: " + e.getMessage(), "500");
        }
    }

}
