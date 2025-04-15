package mx.edu.utez.sacit.controller;

import mx.edu.utez.sacit.dto.RequiredDocumentsDto;
import mx.edu.utez.sacit.service.RequiredDocumentService;
import mx.edu.utez.sacit.service.TransactionLogService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/required-documents")
public class RequiredDocumentController {

    private final RequiredDocumentService requiredDocumentService;
    private final TransactionLogService transactionLogService;

    private static final Logger logger = LogManager.getLogger(RequiredDocumentController.class);
    private static final String OBTENER = "OBTENER";
    private static final String ENTITY_TYPE = "requiered_documents";

    public RequiredDocumentController(RequiredDocumentService requiredDocumentService, TransactionLogService transactionLogService) {
        this.requiredDocumentService = requiredDocumentService;
        this.transactionLogService = transactionLogService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getAll() {
        try {
            logger.info("Inicio de consulta de todos los documentos requeridos.");
            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Documentos");
            ResponseEntity<?> response = requiredDocumentService.findAll();
            logger.info("Documentos requeridos obtenidos exitosamente.");
            return response;
        } catch (Exception e) {
            logger.error("Error al obtener todos los documentos requeridos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al obtener documentos requeridos.");
        }
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getOne(@PathVariable UUID uuid) {
        try {
            logger.info("Inicio de consulta de documento requerido con UUID: {}", uuid);
            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Documento_UUID");
            ResponseEntity<?> response = requiredDocumentService.findByUuid(uuid);
            logger.info("Documento requerido con UUID {} obtenido exitosamente.", uuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al obtener documento requerido con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al obtener el documento requerido.");
        }
    }

    @GetMapping("/procedure/{procedureUuid}")
    public ResponseEntity<?> getByProcedure(@PathVariable UUID procedureUuid) {
        try {
            logger.info("Inicio de consulta de documentos requeridos para el trámite con UUID: {}", procedureUuid);
            transactionLogService.logTransaction(OBTENER, ENTITY_TYPE, "Obtener_Documento_UUID_Procedure");
            ResponseEntity<?> response = requiredDocumentService.findByProcedure(procedureUuid);
            logger.info("Documentos requeridos para trámite con UUID {} obtenidos exitosamente.", procedureUuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al obtener documentos requeridos para trámite con UUID {}: {}", procedureUuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al obtener documentos requeridos para el trámite.");
        }
    }

    @PostMapping("/procedure/{procedureUuid}")
    public ResponseEntity<?> save(@RequestBody RequiredDocumentsDto documentDto, @PathVariable UUID procedureUuid) {
        try {
            logger.info("Inicio de registro de documento requerido para el trámite con UUID: {}", procedureUuid);
            transactionLogService.logTransaction("REGISTRAR", ENTITY_TYPE, "Documento_Procedure_Creado");
            ResponseEntity<?> response = requiredDocumentService.save(documentDto, procedureUuid);
            logger.info("Documento requerido para trámite con UUID {} registrado exitosamente.", procedureUuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al registrar documento requerido para trámite con UUID {}: {}", procedureUuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al registrar documento requerido para el trámite.");
        }
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody RequiredDocumentsDto documentDto) {
        try {
            logger.info("Inicio de actualización de documento requerido con UUID: {}", uuid);
            transactionLogService.logTransaction("ACTUALIZAR", ENTITY_TYPE, "Documento_Actualizado");
            ResponseEntity<?> response = requiredDocumentService.update(uuid, documentDto);
            logger.info("Documento requerido con UUID {} actualizado exitosamente.", uuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al actualizar documento requerido con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al actualizar el documento requerido.");
        }
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        try {
            logger.info("Inicio de eliminación de documento requerido con UUID: {}", uuid);
            transactionLogService.logTransaction("ELIMINAR", ENTITY_TYPE, "Documento_Eliminado");
            ResponseEntity<?> response = requiredDocumentService.delete(uuid);
            logger.info("Documento requerido con UUID {} eliminado exitosamente.", uuid);
            return response;
        } catch (Exception e) {
            logger.error("Error al eliminar documento requerido con UUID {}: {}", uuid, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error al eliminar el documento requerido.");
        }
    }
}
