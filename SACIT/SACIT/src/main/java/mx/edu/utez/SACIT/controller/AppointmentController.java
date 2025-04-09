package mx.edu.utez.SACIT.controller;

import mx.edu.utez.SACIT.dto.AppointmentDto;
import mx.edu.utez.SACIT.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = {"*"})
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/")
    public ResponseEntity<?> getAll() {
        return appointmentService.findAll();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getOne(@PathVariable UUID uuid) {
        return appointmentService.findByUuid(uuid);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getByUser(@PathVariable Integer userId) {
        return appointmentService.findByUser(userId);
    }

    @GetMapping("/procedure/{procedureUuid}")
    public ResponseEntity<?> getByProcedure(@PathVariable UUID procedureUuid) {
        return appointmentService.findByProcedure(procedureUuid);
    }

    @PostMapping("/user/{userId}/procedure/{procedureUuid}/window/{windowId}")
    public ResponseEntity<?> save(
            @RequestBody AppointmentDto appointmentDto,
            @PathVariable Integer userId,
            @PathVariable UUID procedureUuid,
            @PathVariable Integer windowId) {
        return appointmentService.save(appointmentDto, userId, procedureUuid, windowId);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody AppointmentDto appointmentDto) {
        return appointmentService.update(uuid, appointmentDto);
    }

    @PatchMapping("/{uuid}/cancel")
    public ResponseEntity<?> cancel(@PathVariable UUID uuid, @RequestBody CancellationDto cancellationDto) {
        return appointmentService.cancel(uuid, cancellationDto.getCancellationReason());
    }

    @PatchMapping("/{uuid}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID uuid, @RequestBody StatusUpdateDto statusDto) {
        return appointmentService.updateStatus(uuid, statusDto.getStatus());
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        return appointmentService.delete(uuid);
    }

    // DTOs adicionales para el controlador

    private static class CancellationDto {
        private String cancellationReason;

        public String getCancellationReason() {
            return cancellationReason;
        }

        public void setCancellationReason(String cancellationReason) {
            this.cancellationReason = cancellationReason;
        }
    }

    private static class StatusUpdateDto {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}