package mx.edu.utez.SACIT.service;

import jakarta.transaction.Transactional;

import mx.edu.utez.SACIT.dto.WindowDTO;
import mx.edu.utez.SACIT.model.UserModel;
import mx.edu.utez.SACIT.model.Window;
import mx.edu.utez.SACIT.repository.UserRepository;
import mx.edu.utez.SACIT.repository.WindowRepository;


import mx.edu.utez.SACIT.utils.Utilities;
import org.springframework.context.annotation.Primary;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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

    public WindowService(WindowRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public ResponseEntity<?>getAll() {
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
                return Utilities.ResponseWithData(HttpStatus.OK, "Record found successfully.", "200",window);

            } else {
                return Utilities.generateResponse(HttpStatus.NOT_FOUND, "Record not found.", "404");

            }
        } catch (IllegalArgumentException e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid UUID format.", "400");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }

    }


    public ResponseEntity<?> save(WindowDTO dto) {

        try {
            UserModel attendant = userRepository.findByUuid(dto.getAttendantUuid())
                    .orElseThrow(() -> new RuntimeException("User not found"));

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

                return Utilities.ResponseWithData(HttpStatus.OK, "Record created successfully.", "200",window);
            } else {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Invalid role", "400");
            }

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

    public ResponseEntity<?> changeStatus(WindowDTO window) {

        try {
            UserModel attendant = userRepository.findByUuid(window.getAttendantUuid())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (attendant.getRole().getId() == 3 || "ROLE_WINDOW".equals(attendant.getRole().getRole())) {
                Integer nextWindowNumber = repository.findMaxWindowNumber().orElse(0) + 1;
                Window window1 = new Window();
                window1.setStatus(window.getStatus());
                window1.setAttendant(attendant);
                window.setWindowNumber(nextWindowNumber);
                repository.save(window1);
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

}
