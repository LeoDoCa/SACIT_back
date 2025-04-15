package mx.edu.utez.sacit.controller;

import mx.edu.utez.sacit.dto.ProceduresDto;
import mx.edu.utez.sacit.service.ProcedureService;
import mx.edu.utez.sacit.service.TransactionLogService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/procedures")
public class ProcedureController {

    private final ProcedureService procedureService;
    private final TransactionLogService transactionLogService;
    private static final Logger logger = LogManager.getLogger(ProcedureController.class);
    private static final String OBTENER = "OBTENER";
    private static final String ENTITY_TYPE = "procedures";



    public ProcedureController(ProcedureService procedureService, TransactionLogService transactionLogService) {
        this.procedureService = procedureService;
        this.transactionLogService = transactionLogService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            logger.info("Inicio de consulta de todos los procedimientos.");
            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Procedures");
            ResponseEntity<?> response = procedureService.findAll();
            logger.info("Procedimientos obtenidos exitosamente.");
            return response;
        } catch (Exception e) {
            logger.error("Error al obtener todos los procedimientos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al obtener todos los procedimientos.");
        }
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getOne(@PathVariable UUID uuid) {
        try {
            logger.info("Inicio de consulta del procedimiento con UUID: {}", uuid);
            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Procedure_UUID");
            ResponseEntity<?> response = procedureService.findByUuid(uuid);
            logger.info("Procedimiento con UUID {} obtenido exitosamente.", uuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al obtener procedimiento con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al obtener el procedimiento.");
        }
    }

    @PostMapping("/new")
    public ResponseEntity<?> save(@RequestBody ProceduresDto proceduresDto) {
        try {
            logger.info("Inicio del registro de un nuevo procedimiento.");
            transactionLogService.logTransaction("REGISTRAR", ENTITY_TYPE, "Procedure_Creado");
            ResponseEntity<?> response = procedureService.save(proceduresDto);
            logger.info("Procedimiento registrado exitosamente.");
            return response;
        } catch (Exception e) {
            logger.error("Error al registrar un nuevo procedimiento: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al registrar el procedimiento.");
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody ProceduresDto proceduresDto) {
        try {
            logger.info("Inicio de actualización del procedimiento con UUID: {}", uuid);
            transactionLogService.logTransaction("ACTUALIZAR", ENTITY_TYPE, "Procedure_Actualizado");
            ResponseEntity<?> response = procedureService.update(uuid, proceduresDto);
            logger.info("Procedimiento con UUID {} actualizado exitosamente.", uuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al actualizar procedimiento con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al actualizar el procedimiento.");
        }
    }

    @PatchMapping("/{uuid}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID uuid, @RequestBody String status) {
        try {
            logger.info("Inicio de actualización de estado del procedimiento con UUID: {}", uuid);
            ResponseEntity<?> response = procedureService.changeStatus(uuid, status);
            logger.info("Estado del procedimiento con UUID {} actualizado exitosamente.", uuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al actualizar estado del procedimiento con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al actualizar el estado del procedimiento.");
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        try {
            logger.info("Inicio de eliminación del procedimiento con UUID: {}", uuid);
            transactionLogService.logTransaction("ELIMINACION", ENTITY_TYPE, "Elimina_Procedures");
            ResponseEntity<?> response = procedureService.delete(uuid);
            logger.info("Procedimiento con UUID {} eliminado exitosamente.", uuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al eliminar procedimiento con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al eliminar el procedimiento.");
        }
    }
}