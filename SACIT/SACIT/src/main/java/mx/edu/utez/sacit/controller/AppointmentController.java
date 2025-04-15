package mx.edu.utez.sacit.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mx.edu.utez.sacit.dto.AppointmentDto;
import mx.edu.utez.sacit.dto.UnloggedUserDto;
import mx.edu.utez.sacit.dto.UploadedDocumentsDto;
import mx.edu.utez.sacit.model.Procedures;
import mx.edu.utez.sacit.model.UserModel;
import mx.edu.utez.sacit.service.*;

import mx.edu.utez.sacit.service.email.EmailService;
import mx.edu.utez.sacit.utils.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final TransactionLogService transactionLogService;
    private final UserService userService;
    private final ProcedureService procedureService;
    private final EmailService emailService;
    private static final Logger logger = LogManager.getLogger(AppointmentController.class);
    private static final String OBTENER = "OBTENER";
    private static final String ENTITY_TYPE = "appointments";

    private final UploadedDocumentsService uploadedDocumentService;

    public AppointmentController(AppointmentService appointmentService, UploadedDocumentsService uploadedDocumentService, TransactionLogService transactionLogService, UserService userService, ProcedureService procedureService, EmailService emailService) {
        this.appointmentService = appointmentService;
        this.uploadedDocumentService = uploadedDocumentService;
        this.transactionLogService = transactionLogService;
        this.userService = userService;
        this.procedureService = procedureService;
        this.emailService = emailService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Appointments");
            logger.info("Obteniendo todas las citas.");
            return appointmentService.findAll();
        } catch (Exception e) {
            logger.error("Error al obtener todas las citas: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Error al obtener las citas.");
        }
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getOne(@PathVariable UUID uuid) {
        try {
            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Appointment_UUID");
            logger.info("Obteniendo cita con UUID {}.", uuid);
            return appointmentService.findByUuid(uuid);
        } catch (Exception e) {
            logger.error("Error al obtener cita con UUID {}: {}", uuid, e.getMessage());
            return ResponseEntity.internalServerError().body("Error al obtener la cita.");
        }
    }

    @GetMapping("/user/{userUuid}")
    public ResponseEntity<?> getAppointmentsByUser(@PathVariable UUID userUuid) {
        try {
            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Appointment_User_UUID");
            logger.info("Obteniendo citas para el usuario con UUID {}.", userUuid);
            return appointmentService.findByUser(userUuid);
        } catch (Exception e) {
            logger.error("Error al obtener citas para usuario con UUID {}: {}", userUuid, e.getMessage());
            return ResponseEntity.internalServerError().body("Error al obtener las citas del usuario.");
        }
    }

    @GetMapping("/user/{userUuid}/pending")
    public ResponseEntity<?> getPendingAppointmentsByUser(@PathVariable UUID userUuid) {
        try {
            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Appointment_User_UUID_Pending");
            logger.info("Obteniendo citas pendientes para el usuario con UUID {}.", userUuid);
            return appointmentService.findPendingAppointmentsByUser(userUuid);
        } catch (Exception e) {
            logger.error("Error al obtener citas pendientes para usuario con UUID {}: {}", userUuid, e.getMessage());
            return ResponseEntity.internalServerError().body("Error al obtener citas pendientes del usuario.");
        }
    }

    @GetMapping("/procedure/{procedureUuid}")
    public ResponseEntity<?> getByProcedure(@PathVariable UUID procedureUuid) {
        try {
            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Appointment_Procedure_UUID");
            logger.info("Obteniendo citas para el procedimiento con UUID {}.", procedureUuid);
            return appointmentService.findByProcedure(procedureUuid);
        } catch (Exception e) {
            logger.error("Error al obtener citas para procedimiento con UUID {}: {}", procedureUuid, e.getMessage());
            return ResponseEntity.internalServerError().body("Error al obtener las citas del procedimiento.");
        }
    }

    @PostMapping(value = "/", consumes = "multipart/form-data")
    public ResponseEntity<?> saveWithDocuments(
            @RequestParam(required = false) UUID userUuid,
            @RequestParam UUID procedureUuid,
            MultipartHttpServletRequest request) {

        try {
            logger.info("Inicio del método saveWithDocuments para registrar una cita.");

            String appointmentJson = request.getParameter("appointment");
            logger.debug("Datos de la cita recibidos: {}", appointmentJson);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            AppointmentDto appointmentDto = objectMapper.readValue(appointmentJson, AppointmentDto.class);

            if (appointmentDto.getStartTime().isBefore(LocalTime.of(9, 0)) ||
                    appointmentDto.getStartTime().isAfter(LocalTime.of(15, 0))) {
                logger.warn("Horario fuera del rango permitido: {}", appointmentDto.getStartTime());
                return ResponseEntity.badRequest().body("Horario fuera del rango permitido (9:00-15:00)");
            }

            UnloggedUserDto unloggedUserDto = null;
            if (userUuid == null) {
                String unloggedUserJson = request.getParameter("unloggedUser");
                if (unloggedUserJson == null || unloggedUserJson.isEmpty()) {
                    logger.warn("Información de usuario no logueado no proporcionada.");
                    return ResponseEntity.badRequest().body("Se requiere información de usuario no logueado");
                }
                unloggedUserDto = objectMapper.readValue(unloggedUserJson, UnloggedUserDto.class);
            }

            List<MultipartFile> files = request.getFiles("files").stream()
                    .filter(file -> file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty())
                    .collect(Collectors.toList());
            logger.debug("Cantidad de archivos recibidos: {}", files.size());

            String[] documentUuidsArray = request.getParameterValues("documentUuids");
            List<String> documentUuids = documentUuidsArray != null ? Arrays.asList(documentUuidsArray) : Collections.emptyList();

            if (files.size() != documentUuids.size()) {
                logger.warn("Mismatch entre la cantidad de archivos y UUIDs de documentos.");
                return ResponseEntity.badRequest().body("La cantidad de archivos y UUIDs de documentos no coinciden");
            }

            Set<String> documentUuidSet = new HashSet<>(documentUuids);
            if (documentUuidSet.size() != documentUuids.size()) {
                logger.warn("UUIDs duplicados detectados en documentos.");
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

            ResponseEntity<?> saveResponse = appointmentService.saveWithDocuments(
                    appointmentDto,
                    userUuid,
                    unloggedUserDto,
                    procedureUuid,
                    documentDtos
            );

            if (saveResponse.getStatusCode().is2xxSuccessful()) {
                try {
                    String procedureName = getProcedureName(procedureUuid);

                    String recipientEmail;
                    String recipientName;

                    if (userUuid != null) {
                        UserModel user = userService.findByUuid(userUuid);
                        recipientEmail = user.getEmail();
                        recipientName = user.getName();
                    } else {
                        recipientEmail = unloggedUserDto.getEmail();
                        recipientName = unloggedUserDto.getName();
                    }

                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    String formattedDate = appointmentDto.getDate().format(dateFormatter);

                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                    String formattedTime = appointmentDto.getStartTime().format(timeFormatter);

                    String title = "Confirmación de Cita - Sistema SACIT";
                    String subject = "Detalles de tu cita programada en SACIT";
                    String message = "<h2>¡Hola " + recipientName + "!</h2>" +
                            "<p>Tu cita ha sido programada exitosamente en el sistema SACIT.</p>" +
                            "<h3>Detalles de la cita:</h3>" +
                            "<ul>" +
                            "<li><strong>Trámite:</strong> " + procedureName + "</li>" +
                            "<li><strong>Fecha:</strong> " + formattedDate + "</li>" +
                            "<li><strong>Hora:</strong> " + formattedTime + "</li>" +
                            "</ul>" +
                            "<p>Por favor, asegúrate de llegar puntualmente a tu cita.</p>" +
                            "<h3>Atentamente,</h3>" +
                            "<h3>El equipo de SACIT</h3>";

                    emailService.sendSimpleEmail(recipientEmail, title, subject, message);
                    logger.info("Correo de confirmación enviado exitosamente a: {}", recipientEmail);
                } catch (Exception e) {
                    logger.error("Error al enviar correo de confirmación: {}", e.getMessage(), e);
                }
            }

            transactionLogService.logTransaction("REGISTRO", ENTITY_TYPE, "Appointment_Creado");
            logger.info("Cita registrada exitosamente.");
            return saveResponse;
        } catch (IOException e) {
            logger.error("Error al procesar los archivos: {}", e.getMessage(), e);
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Error al procesar los archivos: " + e.getMessage(), "400");
        } catch (IllegalArgumentException e) {
            logger.error("Formato inválido de UUID: {}", e.getMessage(), e);
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Formato inválido de UUID: " + e.getMessage(), "400");
        } catch (Exception e) {
            logger.error("Error interno al registrar la cita: {}", e.getMessage(), e);
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno: " + e.getMessage(), "500");
        }
    }

    private String getProcedureName(UUID procedureUuid) {
        try {
            logger.info("Inicio de obtención del nombre del trámite para UUID: {}", procedureUuid);

            ResponseEntity<?> response = procedureService.findByUuid(procedureUuid);
            logger.debug("Respuesta obtenida del servicio de trámites: {}", response);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object responseBody = response.getBody();

                if (responseBody instanceof Map) {
                    Map<String, Object> responseMap = (Map<String, Object>) responseBody;
                    logger.debug("Cuerpo de respuesta como mapa: {}", responseMap);

                    if (responseMap.containsKey("data")) {
                        Object dataObject = responseMap.get("data");
                        if (dataObject instanceof Procedures) {
                            Procedures procedure = (Procedures) dataObject;
                            logger.info("Nombre del trámite obtenido: {}", procedure.getName());
                            return procedure.getName();
                        }
                    }
                } else if (responseBody instanceof Procedures) {
                    Procedures procedure = (Procedures) responseBody;
                    logger.info("Nombre del trámite obtenido directamente: {}", procedure.getName());
                    return procedure.getName();
                }
            }

            logger.warn("Nombre del trámite no especificado para UUID: {}", procedureUuid);
            return "Trámite no especificado";
        } catch (Exception e) {
            logger.error("Error al obtener el nombre del trámite: {}", e.getMessage(), e);
            return "Trámite no especificado";
        }
    }

    @GetMapping("/{appointmentUuid}/documents")
    public ResponseEntity<?> getDocumentsForAppointment(@PathVariable UUID appointmentUuid) {
        try {
            logger.info("Inicio de obtención de documentos para la cita con UUID: {}", appointmentUuid);

            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Appointment_Documents_UUID");
            logger.debug("Registro de transacción completado para UUID: {}", appointmentUuid);

            ResponseEntity<?> response = uploadedDocumentService.getUploadedDocumentsForAppointment(appointmentUuid);
            logger.info("Documentos obtenidos exitosamente para la cita con UUID: {}", appointmentUuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al obtener documentos para la cita con UUID: {}", appointmentUuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener los documentos.");
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody AppointmentDto appointmentDto) {
        try {
            logger.info("Inicio de actualización de cita con UUID: {}", uuid);
            transactionLogService.logTransaction("ACTUALIZAR", ENTITY_TYPE, "Actualizar_Appointment");
            ResponseEntity<?> response = appointmentService.update(uuid, appointmentDto);
            logger.info("Cita actualizada exitosamente con UUID: {}", uuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al actualizar la cita con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar la cita.");
        }
    }

    @PatchMapping("/{uuid}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID uuid, @RequestBody AppointmentDto appointmentDto) {
        try {
            logger.info("Inicio de actualización de estado para cita con UUID: {}", uuid);
            ResponseEntity<?> response = appointmentService.updateStatus(uuid, appointmentDto.getStatus());
            logger.info("Estado de cita actualizado exitosamente con UUID: {}", uuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al actualizar estado de cita con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar estado de cita.");
        }
    }

    @GetMapping("/today")
    public ResponseEntity<?> getAppointmentsForToday() {
        try {
            logger.info("Inicio de obtención de citas programadas para hoy.");
            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Appointment_Today");
            ResponseEntity<?> response = appointmentService.findAppointmentsByToday();
            logger.info("Citas para hoy obtenidas exitosamente.");
            return response;
        } catch (Exception e) {
            logger.error("Error al obtener citas programadas para hoy: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener citas programadas para hoy.");
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        try {
            logger.info("Inicio de eliminación de cita con UUID: {}", uuid);
            transactionLogService.logTransaction("ELIMINACION", ENTITY_TYPE, "Appointment_Eliminado");
            ResponseEntity<?> response = appointmentService.delete(uuid);
            logger.info("Cita eliminada exitosamente con UUID: {}", uuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al eliminar cita con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar la cita.");
        }
    }

}