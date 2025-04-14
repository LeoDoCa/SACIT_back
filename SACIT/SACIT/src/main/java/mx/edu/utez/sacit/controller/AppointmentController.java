package mx.edu.utez.sacit.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mx.edu.utez.sacit.dto.AppointmentDto;
import mx.edu.utez.sacit.dto.UnloggedUserDto;
import mx.edu.utez.sacit.dto.UploadedDocumentsDto;
import mx.edu.utez.sacit.service.AppointmentService;
import mx.edu.utez.sacit.service.UploadedDocumentsService;
import mx.edu.utez.sacit.service.TransactionLogService;

import mx.edu.utez.sacit.utils.Utilities;
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
    private final TransactionLogService transactionLogService;

    private final UploadedDocumentsService uploadedDocumentService;

    public AppointmentController(AppointmentService appointmentService, UploadedDocumentsService uploadedDocumentService, TransactionLogService transactionLogService) {
        this.appointmentService = appointmentService;
        this.uploadedDocumentService = uploadedDocumentService;
        this.transactionLogService = transactionLogService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        transactionLogService.logTransaction("OBTENER", "appointments" , "Obtener_Appointments");
        return appointmentService.findAll();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getOne(@PathVariable UUID uuid) {
        transactionLogService.logTransaction("OBTENER", "appointments", "Obtener_Appointment_UUID");
        return appointmentService.findByUuid(uuid);
    }

    @GetMapping("/user/{userUuid}")
    public ResponseEntity<?> getAppointmentsByUser(@PathVariable UUID userUuid) {
        transactionLogService.logTransaction("OBTENER", "appointments", "Obtener_Appointment_User_UUID");
        return appointmentService.findByUser(userUuid);
    }

    @GetMapping("/user/{userUuid}/pending")
    public ResponseEntity<?> getPendingAppointmentsByUser(@PathVariable UUID userUuid) {
        transactionLogService.logTransaction("OBTENER", "appointments", "Obtener_Appointment_User_UUID_Pending");
        return appointmentService.findPendingAppointmentsByUser(userUuid);
    }

    @GetMapping("/procedure/{procedureUuid}")
    public ResponseEntity<?> getByProcedure(@PathVariable UUID procedureUuid) {
        transactionLogService.logTransaction("OBTENER", "appointments", "Obtener_Appointment_Procedure_UUID");
        return appointmentService.findByProcedure(procedureUuid);
    }

    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ResponseEntity<?> saveWithDocuments(
            @RequestParam(required = false) UUID userUuid,
            @RequestParam UUID procedureUuid,
            MultipartHttpServletRequest request) {

        try {
            String appointmentJson = request.getParameter("appointment");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            AppointmentDto appointmentDto = objectMapper.readValue(appointmentJson, AppointmentDto.class);

            if (appointmentDto.getStartTime().isBefore(LocalTime.of(9, 0)) ||
                    appointmentDto.getStartTime().isAfter(LocalTime.of(15, 0))) {
                return ResponseEntity.badRequest().body("Horario fuera del rango permitido (9:00-15:00)");
            }

            UnloggedUserDto unloggedUserDto = null;
            if (userUuid == null) {
                String unloggedUserJson = request.getParameter("unloggedUser");
                if (unloggedUserJson == null || unloggedUserJson.isEmpty()) {
                    return ResponseEntity.badRequest().body("Se requiere información de usuario no logueado");
                }
                unloggedUserDto = objectMapper.readValue(unloggedUserJson, UnloggedUserDto.class);
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
                MultipartFile file = files.get(i);

                UploadedDocumentsDto docDto = new UploadedDocumentsDto();
                docDto.setRequiredDocumentUuid(UUID.fromString(documentUuids.get(i)));
                docDto.setDocument(file.getBytes());
                docDto.setFileName(file.getOriginalFilename());
                documentDtos.add(docDto);
            }
            transactionLogService.logTransaction("REGISTRO", "appointments","Appointment_Cread0");
            return appointmentService.saveWithDocuments(
                    appointmentDto,
                    userUuid,
                    unloggedUserDto,
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
        transactionLogService.logTransaction("OBTENER", "appointments", "Obtener_Appointment_Documents_UUID");
        return uploadedDocumentService.getUploadedDocumentsForAppointment(appointmentUuid);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody AppointmentDto appointmentDto) {
        transactionLogService.logTransaction("ACTUALIZAR", "appointments","Actualizar_Appointment");
        return appointmentService.update(uuid, appointmentDto);
    }

    @PatchMapping("/{uuid}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID uuid, @RequestBody AppointmentDto appointmentDto) {
        return appointmentService.updateStatus(uuid, appointmentDto.getStatus());
    }

    @GetMapping("/today")
    public ResponseEntity<?> getAppointmentsForToday() {
        transactionLogService.logTransaction("OBTENER", "appointments", "Obtener_Appointment_Today");
        return appointmentService.findAppointmentsByToday();
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        transactionLogService.logTransaction("ELIMINACION", "appointments", "Appointment_Eliminado");
        return appointmentService.delete(uuid);
    }

}