package mx.edu.utez.sacit.controller;

import mx.edu.utez.sacit.dto.WindowDTO;

import mx.edu.utez.sacit.service.WindowService;
import mx.edu.utez.sacit.service.TransactionLogService;
import mx.edu.utez.sacit.utils.Utilities;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class WindowController {
    private final WindowService service;
    private final TransactionLogService transactionLogService;

    private static final Logger logger = LogManager.getLogger(WindowController.class);
    private static final String OBTENER = "OBTENER";
    private static final String ENTITY_TYPE = "windows";



    public WindowController(WindowService service, TransactionLogService transactionLogService) {
        this.service = service;
        this.transactionLogService = transactionLogService;

    }

    @GetMapping("/window")
    public ResponseEntity<?> windows() {
        try {
            logger.info("Inicio de consulta de todas las ventanillas.");
            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Ventanilla");
            ResponseEntity<?> response = service.getAll();
            logger.info("Ventanillas obtenidas exitosamente.");
            return response;
        } catch (Exception e) {
            logger.error("Error al obtener ventanillas: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al obtener ventanillas.");
        }
    }

    @GetMapping("/window/{uuid}")
    public ResponseEntity<?> getOne(@PathVariable UUID uuid) {
        try {
            logger.info("Inicio de consulta de ventanilla con UUID: {}", uuid);
            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Ventanilla_por_UUID");
            ResponseEntity<?> response = this.service.findByUuid(uuid);
            logger.info("Ventanilla con UUID {} obtenida exitosamente.", uuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al obtener ventanilla con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al obtener la ventanilla.");
        }
    }

    @PostMapping("/window")
    public ResponseEntity<Object> save(@RequestBody WindowDTO window) {
        try {
            if (window != null) {
                logger.info("Inicio de registro de una nueva ventanilla.");
                ResponseEntity<Object> createdWindow = service.save(window);
                transactionLogService.logTransaction("CREACION", ENTITY_TYPE, "Ventanilla_Creada");
                logger.info("Ventanilla registrada exitosamente.");
                return Utilities.ResponseWithData(HttpStatus.OK, "Record created successfully.", "200", createdWindow);
            }
            logger.warn("Los campos requeridos para registrar la ventanilla están incompletos.");
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "All fields are required.", "400");
        } catch (Exception e) {
            logger.error("Error al registrar una nueva ventanilla: {}", e.getMessage(), e);
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }

    @PutMapping("/window/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody WindowDTO window) {
        try {
            if (window != null) {
                logger.info("Inicio de actualización de ventanilla con UUID: {}", uuid);
                transactionLogService.logTransaction("ACTUALIZACION", ENTITY_TYPE, "Ventanilla_Actualizada");
                ResponseEntity<?> response = service.changeStatus(window, uuid);
                logger.info("Ventanilla con UUID {} actualizada exitosamente.", uuid);
                return response;
            }
            logger.warn("Los campos requeridos para actualizar la ventanilla están incompletos.");
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "All fields are required.", "400");
        } catch (Exception e) {
            logger.error("Error al actualizar ventanilla con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al actualizar la ventanilla.");
        }
    }

    @PutMapping("/window/attendant/{uuid}")
    public ResponseEntity<?> updateAttendant(@PathVariable UUID uuid, @RequestBody WindowDTO window) {
        try {
            if (window != null) {
                logger.info("Inicio de cambio de responsable de ventanilla con UUID: {}", uuid);
                transactionLogService.logTransaction("ACTUALIZACION", ENTITY_TYPE, "Cambio_responsable");
                ResponseEntity<?> response = service.changeAttendant(window, uuid);
                logger.info("Responsable de la ventanilla con UUID {} cambiado exitosamente.", uuid);
                return response;
            }
            logger.warn("Los campos requeridos para cambiar el responsable están incompletos.");
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "All fields are required.", "400");
        } catch (Exception e) {
            logger.error("Error al cambiar el responsable de la ventanilla con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al cambiar el responsable de la ventanilla.");
        }
    }

    @DeleteMapping("/window/{uuid}")
    public ResponseEntity<?> delete(@PathVariable("uuid") UUID uuid) {
        try {
            logger.info("Inicio de eliminación de ventanilla con UUID: {}", uuid);
            transactionLogService.logTransaction("ELIMINACION", ENTITY_TYPE, "Ventanilla_Eliminada");
            service.delete(uuid);
            logger.info("Ventanilla con UUID {} eliminada exitosamente.", uuid);
            return Utilities.generateResponse(HttpStatus.OK, "Record deleted successfully.", "200");
        } catch (Exception e) {
            logger.error("Error al eliminar ventanilla con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al eliminar la ventanilla.");
        }
    }

    @GetMapping("/window/{windowUuid}/appointments")
    public ResponseEntity<?> getAppointmentsByWindow(@PathVariable UUID windowUuid) {
        try {
            logger.info("Inicio de consulta de citas asociadas a la ventanilla con UUID: {}", windowUuid);
            ResponseEntity<?> response = service.findAppointmentsByWindow(windowUuid);
            logger.info("Citas asociadas a la ventanilla con UUID {} obtenidas exitosamente.", windowUuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al obtener citas asociadas a la ventanilla con UUID {}: {}", windowUuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al obtener citas asociadas a la ventanilla.");
        }
    }

}
