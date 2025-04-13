package mx.edu.utez.SACIT.service;

import mx.edu.utez.SACIT.model.Appointments;
import mx.edu.utez.SACIT.model.RequiredDocuments;
import mx.edu.utez.SACIT.model.UploadedDocuments;
import mx.edu.utez.SACIT.repository.AppointmentRepository;
import mx.edu.utez.SACIT.repository.UploadedDocumentRepository;
import mx.edu.utez.SACIT.utils.Utilities;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

            return Utilities.ResponseWithData(HttpStatus.OK, "Documentos recuperados exitosamente", "200", uploadedDocs);

        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error al recuperar documentos: " + e.getMessage(), "500");
        }
    }

}