package mx.edu.utez.sacit.service;

import mx.edu.utez.sacit.dto.AppointmentDto;
import mx.edu.utez.sacit.dto.PendingAppointmentDto;
import mx.edu.utez.sacit.dto.UnloggedUserDto;
import mx.edu.utez.sacit.dto.UploadedDocumentsDto;
import mx.edu.utez.sacit.model.*;
import mx.edu.utez.sacit.repository.*;
import mx.edu.utez.sacit.utils.Utilities;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {


    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ProcedureRepository procedureRepository;
    private final RequiredDocumentRepository requiredDocumentRepository;
    private final UploadedDocumentRepository uploadedDocumentRepository;
    private final UnloggedUsersRepository unloggedUsersRepository;
    private final AvailabilityService availabilityService;

    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository, ProcedureRepository procedureRepository, WindowRepository windowRepository, AvailabilityService availabilityService, RequiredDocumentRepository requiredDocumentRepository, UploadedDocumentRepository uploadedDocumentRepository, UnloggedUsersRepository unloggedUsersRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.procedureRepository = procedureRepository;
        this.availabilityService = availabilityService;
        this.requiredDocumentRepository = requiredDocumentRepository;
        this.uploadedDocumentRepository = uploadedDocumentRepository;
        this.unloggedUsersRepository = unloggedUsersRepository;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findAll() {

        try {
            List<Appointments> appointmentsList = appointmentRepository.findAll();
            if (appointmentsList.isEmpty()) {
                return Utilities.generateResponse(HttpStatus.NO_CONTENT, "No appointments found", "204");
            }
            return Utilities.ResponseWithData(HttpStatus.OK, "Appointments found", "200", appointmentsList);
   } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }

    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findByUuid(UUID uuid) {

        try {
            Appointments appointment = appointmentRepository.findByUuid(uuid);
            if (appointment != null) {
                return Utilities.ResponseWithData(HttpStatus.OK, "Appointment found", "200", appointment);
            } else {
                return Utilities.generateResponse(HttpStatus.NOT_FOUND, "Appointment not found", "404");
            }

        } catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID format", "400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }

    @Transactional
    public ResponseEntity<?> saveWithDocuments(AppointmentDto appointmentDto,
                                               UUID userUuid,
                                               UnloggedUserDto unloggedUserDto,
                                               UUID procedureUuid,
                                               List<UploadedDocumentsDto> documents) {
        try {
            Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(procedureUuid);
            if (optionalProcedure.isEmpty()) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Procedure not found", "400");
            }

            if (appointmentDto.getStartTime().isBefore(LocalTime.of(9, 0)) ||
                    appointmentDto.getStartTime().isAfter(LocalTime.of(15, 0))) {
                throw new RuntimeException("Horario fuera del rango permitido (9:00-15:00)");
            }

            Procedures procedure = optionalProcedure.get();

            List<Appointments> existingAppointments = null;
            if (userUuid != null) {
                existingAppointments = appointmentRepository.findByUserUuidAndDateAndStartTime(
                        userUuid, appointmentDto.getDate(), appointmentDto.getStartTime());
            } else if (unloggedUserDto != null) {
                existingAppointments = appointmentRepository.findByUnloggedUserEmailAndDateAndStartTime(
                        unloggedUserDto.getEmail(), appointmentDto.getDate(), appointmentDto.getStartTime());
            }

            if (existingAppointments != null && !existingAppointments.isEmpty()) {
                return Utilities.generateResponse(HttpStatus.CONFLICT,
                        "Ya tienes una cita agendada en este día a esta hora", "409");
            }

            boolean allMandatoryDocumentsPresent = validateRequiredDocuments(procedure, documents);
            if (!allMandatoryDocumentsPresent) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST,
                        "Not all mandatory documents are present", "400");
            }

            Window window = availabilityService.findAvailableWindow(
                    appointmentDto.getDate(),
                    appointmentDto.getStartTime(),
                    procedure
            );
            if (window == null) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "No available windows", "400");
            }

            LocalTime endTime = appointmentDto.getStartTime()
                    .plusMinutes(procedure.getStimatedTime());

            Appointments appointment = new Appointments();
            appointment.setUuid(UUID.randomUUID());
            appointment.setDate(appointmentDto.getDate());
            appointment.setStartTime(appointmentDto.getStartTime());
            appointment.setEndTime(endTime);
            appointment.setCreationDate(LocalDate.now());
            appointment.setStatus("Pendiente");
            appointment.setProcedure(procedure);
            appointment.setWindow(window);

            if (userUuid != null) {
                Optional<UserModel> optionalUser = userRepository.findByUuid(userUuid);
                if (optionalUser.isEmpty()) {
                    return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "User not found", "400");
                }

                UserModel user = optionalUser.get();

                if (user.getRole().getId() != 1 && !"ROLE_USER".equals(user.getRole().getRole())) {
                    return Utilities.generateResponse(HttpStatus.BAD_REQUEST,
                            "Only users with ROLE_USER can create appointments", "400");
                }

                appointment.setUser(user);
            } else if (unloggedUserDto != null) {
                UnloggedUsers unloggedUser = new UnloggedUsers();
                unloggedUser.setName(unloggedUserDto.getName());
                unloggedUser.setLastName(unloggedUserDto.getLastName());
                unloggedUser.setEmail(unloggedUserDto.getEmail());

                unloggedUser = unloggedUsersRepository.save(unloggedUser);
                appointment.setUnloggedUser(unloggedUser);
            } else {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST,
                        "Either a logged user or unlogged user information is required", "400");
            }

            appointment = appointmentRepository.save(appointment);

            List<UploadedDocuments> uploadedDocs = new ArrayList<>();
            if (documents != null && !documents.isEmpty()) {
                for (UploadedDocumentsDto docDto : documents) {
                    if (!validateDocumentProperties(docDto)) {
                        return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Document validation failed", "400");
                    }

                    RequiredDocuments requiredDoc = requiredDocumentRepository.findByUuid(docDto.getRequiredDocumentUuid());
                    if (requiredDoc != null) {
                        UploadedDocuments uploadedDoc = new UploadedDocuments();
                        uploadedDoc.setUuid(UUID.randomUUID());
                        uploadedDoc.setUploadedDate(LocalDate.now());
                        uploadedDoc.setDocument(docDto.getDocument());
                        uploadedDoc.setAppointment(appointment);
                        uploadedDoc.setFileName(docDto.getFileName());
                        uploadedDoc.getRequiredDocuments().add(requiredDoc);
                        requiredDoc.getUploadedDocuments().add(uploadedDoc);

                        uploadedDocs.add(uploadedDocumentRepository.save(uploadedDoc));

                        requiredDocumentRepository.save(requiredDoc);
                    }
                }
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("appointment", appointment);

            return Utilities.ResponseWithData(HttpStatus.CREATED, "Appointment created successfully", "200", responseData);

        } catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID format", "400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal Server Error: " + e.getMessage(), "500");
        }
    }

    private boolean validateRequiredDocuments(Procedures procedure, List<UploadedDocumentsDto> documents) {
        Set<RequiredDocuments> requiredDocs = procedure.getRequieredDocuments();

        if (requiredDocs.isEmpty()) {
            return true;
        }

        if (documents == null || documents.isEmpty()) {
            return false;
        }

        Set<UUID> providedDocUuids = documents.stream()
                .map(UploadedDocumentsDto::getRequiredDocumentUuid)
                .collect(Collectors.toSet());

        for (RequiredDocuments requiredDoc : requiredDocs) {
            if (!providedDocUuids.contains(requiredDoc.getUuid())) {
                return false;
            }
        }
        return true;
    }

    private boolean validateDocumentProperties(UploadedDocumentsDto document) {
        try {
            byte[] fileContent = document.getDocument();
            if (fileContent.length < 4) {
                return false;
            }

            String fileHeader = new String(fileContent, 0, 4);
            if (!fileHeader.equals("%PDF")) {
                return false;
            }

            long fileSize = fileContent.length;
            return fileSize <= 512 * 1024;
        } catch (Exception e) {
            System.out.println("Error al validar propiedades del documento: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public ResponseEntity<?> update(UUID uuid, AppointmentDto appointmentDto) {
        Appointments existingAppointment = appointmentRepository.findByUuid(uuid);
        if (existingAppointment == null) {
            return Utilities.generateResponse(HttpStatus.NOT_FOUND, "Appointment not found", "404");
        }

        try {
            existingAppointment.setStartTime(appointmentDto.getStartTime());

            if (appointmentDto.getStatus() != null && !appointmentDto.getStatus().isEmpty()) {
                existingAppointment.setStatus(appointmentDto.getStatus());
            }

            Appointments updatedAppointment = appointmentRepository.save(existingAppointment);
            return Utilities.generateResponse(HttpStatus.OK, "Appointment updated successfully", "200");
        }
        catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid format", HttpStatus.BAD_REQUEST);
        } catch(Exception e){
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }

    @Transactional
    public ResponseEntity<?> updateStatus(UUID uuid, String status) {
        Appointments existingAppointment = appointmentRepository.findByUuid(uuid);
        if (existingAppointment == null) {
            return new ResponseEntity<>("Appointment not found", HttpStatus.NOT_FOUND);
        }

        try {
            existingAppointment.setStatus(status);
            Appointments updatedAppointment = appointmentRepository.save(existingAppointment);
            return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> delete(UUID uuid) {
        Appointments appointment = appointmentRepository.findByUuid(uuid);
        if (appointment == null) {
            return new ResponseEntity<>("Appointment not found", HttpStatus.NOT_FOUND);
        }

        try {
            appointmentRepository.delete(appointment);
            return new ResponseEntity<>("Appointment deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findByUser(UUID userUuid) {
        try {
            Optional<UserModel> optionalUser = userRepository.findByUuid(userUuid);
            if (optionalUser.isEmpty()) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "User not found", "400");
            }

            List<Appointments> appointments = appointmentRepository.findByUserAndStatus(optionalUser.get(), "Finalizada");
            if (appointments.isEmpty()) {
                return Utilities.generateResponse(HttpStatus.NO_CONTENT, "No completed appointments found for this user", "204");
            }

            List<PendingAppointmentDto> appointmentDTOs = appointments.stream().map(appointment -> {
                PendingAppointmentDto dto = new PendingAppointmentDto();

                dto.setId(appointment.getId());
                dto.setUuid(appointment.getUuid());
                dto.setDate(appointment.getDate());
                dto.setStartTime(appointment.getStartTime());
                dto.setEndTime(appointment.getEndTime());
                dto.setCreationDate(appointment.getCreationDate());
                dto.setStatus(appointment.getStatus());

                UserModel user = appointment.getUser();
                if (user != null) {
                    dto.setUserUuid(user.getUuid());
                    dto.setUserName(user.getName());
                    dto.setUserLastName(user.getLastName());
                    dto.setUserEmail(user.getEmail());
                }

                Procedures procedure = appointment.getProcedure();
                if (procedure != null) {
                    dto.setProcedureUuid(procedure.getUuid());
                    dto.setProcedureName(procedure.getName());
                }

                Window window = appointment.getWindow();
                if (window != null) {
                    dto.setWindowUuid(window.getUuid());
                    dto.setWindowNumber(window.getWindowNumber());
                }

                return dto;
            }).toList();

            return Utilities.ResponseWithData(HttpStatus.OK, "Completed appointments found", "200", appointmentDTOs);
        } catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID format", "400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findAppointmentsByToday() {
        try {
            LocalDate today = LocalDate.now();

            List<Appointments> appointments = appointmentRepository.findByDate(today);
            if (appointments.isEmpty()) {
                return Utilities.generateResponse(HttpStatus.NO_CONTENT, "No appointments found for today", "204");
            }

            List<PendingAppointmentDto> appointmentDTOs = appointments.stream().map(appointment -> {
                PendingAppointmentDto dto = new PendingAppointmentDto();

                dto.setId(appointment.getId());
                dto.setUuid(appointment.getUuid());
                dto.setDate(appointment.getDate());
                dto.setStartTime(appointment.getStartTime());
                dto.setEndTime(appointment.getEndTime());
                dto.setCreationDate(appointment.getCreationDate());
                dto.setStatus(appointment.getStatus());

                UserModel user = appointment.getUser();
                if (user != null) {
                    dto.setUserUuid(user.getUuid());
                    dto.setUserName(user.getName());
                    dto.setUserLastName(user.getLastName());
                    dto.setUserEmail(user.getEmail());
                } else if (appointment.getUnloggedUser() != null) { 
                    UnloggedUsers unloggedUser = appointment.getUnloggedUser();
                    dto.setUserName(unloggedUser.getName());
                    dto.setUserLastName(unloggedUser.getLastName());
                    dto.setUserEmail(unloggedUser.getEmail());
                }

                Procedures procedure = appointment.getProcedure();
                if (procedure != null) {
                    dto.setProcedureUuid(procedure.getUuid());
                    dto.setProcedureName(procedure.getName());
                }

                Window window = appointment.getWindow();
                if (window != null) {
                    dto.setWindowUuid(window.getUuid());
                    dto.setWindowNumber(window.getWindowNumber());
                }

                return dto;
            }).toList();

            return Utilities.ResponseWithData(HttpStatus.OK, "Appointments found for today", "200", appointmentDTOs);
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error: " + e.getMessage(), "500");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findPendingAppointmentsByUser(UUID userUuid) {
        try {
            Optional<UserModel> optionalUser = userRepository.findByUuid(userUuid);
            if (optionalUser.isEmpty()) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "User not found", "400");
            }

            List<Appointments> appointments = appointmentRepository.findByUserAndStatus(optionalUser.get(), "Pendiente");
            if (appointments.isEmpty()) {
                return Utilities.generateResponse(HttpStatus.NO_CONTENT, "No pending appointments found for this user", "204");
            }

            List<PendingAppointmentDto> appointmentDTOs = appointments.stream().map(appointment -> {
                PendingAppointmentDto dto = new PendingAppointmentDto();

                dto.setId(appointment.getId());
                dto.setUuid(appointment.getUuid());
                dto.setDate(appointment.getDate());
                dto.setStartTime(appointment.getStartTime());
                dto.setEndTime(appointment.getEndTime());
                dto.setCreationDate(appointment.getCreationDate());
                dto.setStatus(appointment.getStatus());

                UserModel user = appointment.getUser();
                if (user != null) {
                    dto.setUserUuid(user.getUuid());
                    dto.setUserName(user.getName());
                    dto.setUserLastName(user.getLastName());
                    dto.setUserEmail(user.getEmail());
                }

                Procedures procedure = appointment.getProcedure();
                if (procedure != null) {
                    dto.setProcedureUuid(procedure.getUuid());
                    dto.setProcedureName(procedure.getName());
                }

                Window window = appointment.getWindow();
                if (window != null) {
                    dto.setWindowUuid(window.getUuid());
                    dto.setWindowNumber(window.getWindowNumber());
                }

                return dto;
            }).toList();

            return Utilities.ResponseWithData(HttpStatus.OK, "Pending appointments found", "200", appointmentDTOs);
        } catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID format", "400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findByProcedure(UUID procedureUuid) {
        Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(procedureUuid);
        if (!optionalProcedure.isPresent()) {
            return new ResponseEntity<>("Procedure not found", HttpStatus.BAD_REQUEST);
        }

        List<Appointments> appointments = appointmentRepository.findAll().stream()
                .filter(app -> app.getProcedure().getId().equals(optionalProcedure.get().getId()))
                .toList();

        if (appointments.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }
}
