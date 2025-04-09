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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@Transactional
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private WindowRepository windowRepository;

    @Transactional(readOnly = true)
    public ResponseEntity<?> findAll() {
        List<Appointments> appointmentsList = appointmentRepository.findAll();
        if (appointmentsList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(appointmentsList, HttpStatus.OK);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> findByUuid(UUID uuid) {
        Appointments appointment = appointmentRepository.findByUuid(uuid);
        if (appointment != null) {
            return new ResponseEntity<>(appointment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity<?> save(AppointmentDto appointmentDto, Integer userId, UUID procedureUuid, Integer windowId) {
        try {
            Optional<UserModel> optionalUser = userRepository.findById(userId);
            if (!optionalUser.isPresent()) {
                return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
            }

            Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(procedureUuid);
            if (!optionalProcedure.isPresent()) {
                return new ResponseEntity<>("Procedure not found", HttpStatus.BAD_REQUEST);
            }

            Optional<Window> optionalWindow = windowRepository.findById(windowId);
            if (!optionalWindow.isPresent()) {
                return new ResponseEntity<>("Window not found", HttpStatus.BAD_REQUEST);
            }

            Appointments appointment = new Appointments();
            appointment.setUuid(UUID.randomUUID());
            appointment.setPhone(appointmentDto.getPhone());
            appointment.setDate(appointmentDto.getDate());
            appointment.setStartTime(appointmentDto.getStartTime());
            appointment.setEndTime(appointmentDto.getEndTime());
            appointment.setCreationDate(LocalDate.now());
            appointment.setConfirmationCode(generateConfirmationCode());
            appointment.setStatus("PENDING");
            appointment.setUser(optionalUser.get());
            appointment.setProcedure(optionalProcedure.get());
            appointment.setWindow(optionalWindow.get());

            Appointments savedAppointment = appointmentRepository.save(appointment);
            return new ResponseEntity<>(savedAppointment, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> update(UUID uuid, AppointmentDto appointmentDto) {
        Appointments existingAppointment = appointmentRepository.findByUuid(uuid);
        if (existingAppointment == null) {
            return new ResponseEntity<>("Appointment not found", HttpStatus.NOT_FOUND);
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
            return new ResponseEntity<>(updatedAppointment, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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