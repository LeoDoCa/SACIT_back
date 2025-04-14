package mx.edu.utez.sacit.service;

import jakarta.transaction.Transactional;

import mx.edu.utez.sacit.dto.PendingAppointmentDto;
import mx.edu.utez.sacit.dto.WindowDTO;
import mx.edu.utez.sacit.model.*;
import mx.edu.utez.sacit.repository.AppointmentRepository;
import mx.edu.utez.sacit.repository.UserRepository;
import mx.edu.utez.sacit.repository.WindowRepository;


import mx.edu.utez.sacit.utils.Utilities;
import org.springframework.context.annotation.Primary;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Primary
@Transactional
public class WindowService {
    private final WindowRepository repository;
    private final UserRepository userRepository;
    private final WindowRepository windowRepository;
    private final AppointmentRepository appointmentRepository;

    public WindowService(WindowRepository repository, UserRepository userRepository, WindowRepository windowRepository, AppointmentRepository appointmentRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.windowRepository = windowRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public ResponseEntity<?> getAll() {
        try {
            List<Window> windows = repository.findAll();
            if (windows.isEmpty()) {
                return Utilities.generateResponse(HttpStatus.NOT_FOUND, "No records found.", "404");
            } else {
                return Utilities.ResponseWithData(HttpStatus.OK, "Records found successfully.", "200", windows);
            }
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }


    }

    public ResponseEntity<?> findByUuid(UUID uuid) {
        Optional<Window> window = repository.findByUuid(uuid);
        try {

            if (window.isPresent()) {
                return Utilities.ResponseWithData(HttpStatus.OK, "Record found successfully.", "200", window);

            } else {
                return Utilities.generateResponse(HttpStatus.NOT_FOUND, "Record not found.", "404");

            }
        } catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID format.", "400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }

    }


    public ResponseEntity <Object> save(WindowDTO dto) {
        try {
            UserModel attendant = userRepository.findByUuid(dto.getAttendantUuid())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Window existingWindow = windowRepository.findByAttendant_Uuid(attendant.getUuid());
            if (existingWindow != null) {
return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "The user is already assigned to a window.", "400");
            }
            if (attendant.getRole().getId() == 3 || "ROLE_WINDOW".equals(attendant.getRole().getRole())) {
                Integer nextWindowNumber = repository.findMaxWindowNumber().orElse(0) + 1;
                LocalTime startTime = LocalTime.of(9, 0);
                LocalTime endTime = LocalTime.of(15, 0);
                Window window = new Window();
                window.setStatus("Activa");
                window.setAttendant(attendant);
                window.setWindowNumber(nextWindowNumber);
                window.setStartTime(startTime);
                window.setEndTime(endTime);

                 repository.save(window);
                return Utilities.ResponseWithData(HttpStatus.OK, "Record created successfully.", "200", window);
            } else {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid role", "400");   }

        } catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID format.", "400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");

        }
    }

    public ResponseEntity<?> delete(UUID uuid) {
        try {
            Optional<Window> window = repository.findByUuid(uuid);
            if (window.isPresent()) {
                repository.deleteByUuid(uuid);
                return Utilities.generateResponse(HttpStatus.OK, "Record deleted successfully.", "200");
            } else {
                return Utilities.generateResponse(HttpStatus.NOT_FOUND, "Record not found.", "404");
            }
        } catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID format.", "400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }

    public ResponseEntity<?> changeAttendant(WindowDTO window, UUID uuid) {

        try {
            Window existingWindow = repository.findByUuid(uuid)
                    .orElseThrow(() -> new RuntimeException("Window not found"));
            UserModel attendant = userRepository.findByUuid(window.getAttendantUuid())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (attendant.getRole().getId() == 3 || "ROLE_WINDOW".equals(attendant.getRole().getRole())) {

                existingWindow.setAttendant(attendant);
                repository.save(existingWindow);
                return Utilities.generateResponse(HttpStatus.OK, "Record updated successfully.", "200");
            } else {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid role", "400");
            }

        } catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID format.", "400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }

    public ResponseEntity<?> changeStatus(WindowDTO window, UUID uuid) {

        try {
            Window existingWindow = repository.findByUuid(uuid)
                    .orElseThrow(() -> new RuntimeException("Window not found"));

            existingWindow.setStatus(window.getStatus());

            repository.save(existingWindow);
            return Utilities.generateResponse(HttpStatus.OK, "Record updated successfully.", "200");

        } catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID format.", "400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }

    public ResponseEntity<?> findAppointmentsByWindow(UUID windowUuid) {
        try {
            Optional<Window> optionalWindow = windowRepository.findByUuid(windowUuid);
            if (optionalWindow.isEmpty()) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Window not found", "400");
            }

            LocalDate today = LocalDate.now();

            List<Appointments> appointments = appointmentRepository.findByWindowAndDate(optionalWindow.get(), today);
            if (appointments.isEmpty()) {
                return Utilities.generateResponse(HttpStatus.NO_CONTENT, "No appointments found for this window today", "204");
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

                if (appointment.getUser() != null) { 
                    UserModel user = appointment.getUser();
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

                dto.setWindowUuid(windowUuid); 
                dto.setWindowNumber(optionalWindow.get().getWindowNumber());

                return dto;
            }).toList();

            return Utilities.ResponseWithData(HttpStatus.OK, "Appointments found for this window", "200", appointmentDTOs);
        } catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID format", "400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }

}
