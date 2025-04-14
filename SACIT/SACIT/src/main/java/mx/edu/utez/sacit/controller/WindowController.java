package mx.edu.utez.sacit.controller;

import mx.edu.utez.sacit.dto.WindowDTO;

import mx.edu.utez.sacit.service.WindowService;
import mx.edu.utez.sacit.service.TransactionLogService;
import mx.edu.utez.sacit.utils.Utilities;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class WindowController {
    private final WindowService service;
    private final TransactionLogService transactionLogService;



    public WindowController(WindowService service, TransactionLogService transactionLogService) {
        this.service = service;
        this.transactionLogService = transactionLogService;

    }

    @GetMapping("/window")
    public ResponseEntity<?> windows() {
        transactionLogService.logTransaction("OBTENER", "windows", "0", "Obtener_Ventanilla");
        return service.getAll();
    }

    @GetMapping("/window/{uuid}")
    public ResponseEntity<?> getOne(@PathVariable UUID uuid) {
        transactionLogService.logTransaction("OBTENER", "windows", uuid.toString(), "Obtener_Ventanilla_por_UUID");
        return this.service.findByUuid(uuid);
    }

    @PostMapping("/window")
    public ResponseEntity<Object> save(@RequestBody WindowDTO window) {
        try {
            if (window != null) {
                ResponseEntity<Object> createdWindow = service.save(window);
                transactionLogService.logTransaction("CREACION", "windows", window.getWindowNumber().toString(), "Ventanilla_Creada");
                return Utilities.ResponseWithData(HttpStatus.OK, "Record created successfully.", "200", createdWindow);
            }

            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "All fields are required.", "400");
        } catch (Exception e) {

            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }

    @PutMapping("/window/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody WindowDTO window) {
        if (window != null) {
            transactionLogService.logTransaction("ACTUALIZACION", "windows", window.getWindowNumber().toString(), "Ventanilla_Actualizada");
            return service.changeStatus(window, uuid);

        }
        return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "All fields are required.", "400");
    }
    @PutMapping("/window/attendant/{uuid}")
    public ResponseEntity<?> updateAttendant(@PathVariable UUID uuid, @RequestBody WindowDTO window) {
        if (window != null) {
            transactionLogService.logTransaction("ACTUALIZACION", "windows", window.getWindowNumber().toString(), "Cambio_responsable");
            return service.changeAttendant(window, uuid);

        }
        return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "All fields are required.", "400");
    }

    @DeleteMapping("/window/{uuid}")
    public ResponseEntity<?> delete(@PathVariable("uuid") UUID uuid) {
        transactionLogService.logTransaction("ELIMINACION", "windows", uuid.toString(), "Ventanilla_Eliminada");
        service.delete(uuid);
        return Utilities.generateResponse(HttpStatus.OK, "Record deleted successfully.", "200");
    }

    @GetMapping("/window/{windowUuid}/appointments")
    public ResponseEntity<?> getAppointmentsByWindow(@PathVariable UUID windowUuid) {
        return service.findAppointmentsByWindow(windowUuid);
    }

}
