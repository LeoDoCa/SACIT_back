package mx.edu.utez.SACIT.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mx.edu.utez.SACIT.dto.AppointmentDto;
import mx.edu.utez.SACIT.dto.UploadedDocumentsDto;
import mx.edu.utez.SACIT.service.AppointmentService;
import mx.edu.utez.SACIT.service.UploadedDocumentsService;

import mx.edu.utez.SACIT.utils.Utilities;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    private final UploadedDocumentsService uploadedDocumentService;

    public AppointmentController(AppointmentService appointmentService, UploadedDocumentsService uploadedDocumentService) {
        this.appointmentService = appointmentService;
        this.uploadedDocumentService = uploadedDocumentService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getAll() {
        return appointmentService.findAll();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getOne(@PathVariable UUID uuid) {
        return appointmentService.findByUuid(uuid);
    }

    @GetMapping("/user/{userUuid}")
    public ResponseEntity<?> getAppointmentsByUser(@PathVariable UUID userUuid) {
        return appointmentService.findByUser(userUuid);
    }

    @GetMapping("/procedure/{procedureUuid}")
    public ResponseEntity<?> getByProcedure(@PathVariable UUID procedureUuid) {
        return appointmentService.findByProcedure(procedureUuid);
    }

    @PostMapping(value = "/user/{userUuid}/procedure/{procedureUuid}", consumes = "multipart/form-data")
    public ResponseEntity<?> saveWithDocuments(
            @PathVariable UUID userUuid,
            @PathVariable UUID procedureUuid,
            MultipartHttpServletRequest request) {

        try {
            String appointmentJson = request.getParameter("appointment");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            AppointmentDto appointmentDto = objectMapper.readValue(appointmentJson, AppointmentDto.class);

            if (appointmentDto.getStartTime().isBefore(LocalTime.of(9, 0)) ||
                    appointmentDto.getStartTime().isAfter(LocalTime.of(15, 0))) {
                return ResponseEntity.badRequest().body("Horario fuera del rango permitido (9:00-15:00)");
            }

            List<MultipartFile> files = request.getFiles("files").stream()
                    .filter(file -> file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty())
                    .collect(Collectors.toList());

            String[] documentUuidsArray = request.getParameterValues("documentUuids");
            List<String> documentUuids = documentUuidsArray != null ? Arrays.asList(documentUuidsArray) : Collections.emptyList();

            if (files.size() != documentUuids.size()) {
                return ResponseEntity.badRequest().body("La cantidad de archivos y UUIDs de documentos no coinciden");
            }

            Set<String> documentUuidSet = new HashSet<>(documentUuids);
            if (documentUuidSet.size() != documentUuids.size()) {
                return ResponseEntity.badRequest().body("UUIDs duplicados detectados en documentos");
            }

            List<UploadedDocumentsDto> documentDtos = new ArrayList<>();
            for (int i = 0; i < files.size(); i++) {
                UploadedDocumentsDto docDto = new UploadedDocumentsDto();
                docDto.setRequiredDocumentUuid(UUID.fromString(documentUuids.get(i)));
                docDto.setDocument(files.get(i).getBytes());
                documentDtos.add(docDto);
            }

            return appointmentService.saveWithDocuments(
                    appointmentDto,
                    userUuid,
                    procedureUuid,
                    documentDtos
            );
        } catch (IOException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Error al procesar los archivos: " + e.getMessage(), "400");
        } catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Formato inválido de UUID: " + e.getMessage(), "400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno: " + e.getMessage(), "500");
        }
    }

    @GetMapping("/{appointmentUuid}/documents")
    public ResponseEntity<?> getDocumentsForAppointment(@PathVariable UUID appointmentUuid) {
        return uploadedDocumentService.getUploadedDocumentsForAppointment(appointmentUuid);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody AppointmentDto appointmentDto) {
        return appointmentService.update(uuid, appointmentDto);
    }

    @PatchMapping("/{uuid}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID uuid, @RequestBody AppointmentDto appointmentDto) {
        return appointmentService.updateStatus(uuid, appointmentDto.getStatus());
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        return appointmentService.delete(uuid);
    }

}