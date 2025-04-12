package mx.edu.utez.SACIT.service;

import mx.edu.utez.SACIT.dto.AppointmentDto;
import mx.edu.utez.SACIT.model.Appointments;
import mx.edu.utez.SACIT.model.Procedures;
import mx.edu.utez.SACIT.model.UserModel;
import mx.edu.utez.SACIT.model.Window;
import mx.edu.utez.SACIT.repository.AppointmentRepository;
import mx.edu.utez.SACIT.repository.ProcedureRepository;
import mx.edu.utez.SACIT.repository.UserRepository;
import mx.edu.utez.SACIT.repository.WindowRepository;
import mx.edu.utez.SACIT.utils.Utilities;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class AppointmentService {


    private final AppointmentRepository appointmentRepository;


    private final UserRepository userRepository;


    private final ProcedureRepository procedureRepository;


    private final WindowRepository windowRepository;
    private final AvailabilityService availabilityService;

    public AppointmentService(AppointmentRepository appointmentRepository, UserRepository userRepository, ProcedureRepository procedureRepository, WindowRepository windowRepository, AvailabilityService availabilityService) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.procedureRepository = procedureRepository;
        this.windowRepository = windowRepository;
        this.availabilityService = availabilityService;
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
    public ResponseEntity<?> save(AppointmentDto appointmentDto, UUID userUuid, UUID procedureUuid) {
        try {
            Optional<UserModel> optionalUser = userRepository.findByUuid(userUuid);
            if (!optionalUser.isPresent()) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "User not found", "400");
            }

            Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(procedureUuid);
            if (!optionalProcedure.isPresent()) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Procedure not found", "400");

            }
            if (appointmentDto.getStartTime().isBefore(LocalTime.of(9, 0)) ||
                    appointmentDto.getStartTime().isAfter(LocalTime.of(15, 0))) {
                throw new RuntimeException("Horario fuera del rango permitido (9:00-15:00)");
            }

            Procedures procedure = optionalProcedure.get();

            Window window = availabilityService.findAvailableWindow(
                    appointmentDto.getDate(),
                    appointmentDto.getStartTime(),
                    procedure
            );
            if (window == null) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "No available windows", "400");
            }

            LocalTime endTime = appointmentDto.getStartTime()
                    .plusMinutes(procedure.getStimatedTime() + 15);

            Appointments appointment = new Appointments();
            appointment.setUuid(UUID.randomUUID());
            appointment.setDate(appointmentDto.getDate());
            appointment.setStartTime(appointmentDto.getStartTime());
            appointment.setEndTime(endTime);
            appointment.setCreationDate(LocalDate.now());
            appointment.setConfirmationCode(generateConfirmationCode());
            appointment.setStatus("PENDING");
            appointment.setUser(optionalUser.get());
            appointment.setPhone(appointmentDto.getPhone());
            appointment.setProcedure(optionalProcedure.get());
            appointment.setWindow(window);


            appointmentRepository.save(appointment);
            return Utilities.ResponseWithData(HttpStatus.CREATED, "Appointment created successfully", "200",appointment);


        } catch (IllegalArgumentException e) {
            Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID format", "400");
        } catch
        (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
        return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
    }

    @Transactional
    public ResponseEntity<?> update(UUID uuid, AppointmentDto appointmentDto) {
        Appointments existingAppointment = appointmentRepository.findByUuid(uuid);
        if (existingAppointment == null) {
            return Utilities.generateResponse(HttpStatus.NOT_FOUND, "Appointment not found", "404");
        }

        try {
            existingAppointment.setPhone(appointmentDto.getPhone());
            existingAppointment.setDate(appointmentDto.getDate());
            existingAppointment.setStartTime(appointmentDto.getStartTime());
            existingAppointment.setEndTime(appointmentDto.getEndTime());

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
    public ResponseEntity<?> cancel(UUID uuid, String cancellationReason) {
        Appointments existingAppointment = appointmentRepository.findByUuid(uuid);
        if (existingAppointment == null) {
            return new ResponseEntity<>("Appointment not found", HttpStatus.NOT_FOUND);
        }

        try {
            existingAppointment.setCancellationReason(cancellationReason);
            existingAppointment.setStatus("CANCELLED");

            Appointments updatedAppointment = appointmentRepository.save(existingAppointment);
            return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
    public ResponseEntity<?> findByUser(Integer userId) {
        Optional<UserModel> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
        }

        List<Appointments> appointments = appointmentRepository.findAll().stream()
                .filter(app -> app.getUser().getId().equals(userId))
                .toList();

        if (appointments.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(appointments, HttpStatus.OK);
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

    private String generateConfirmationCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}