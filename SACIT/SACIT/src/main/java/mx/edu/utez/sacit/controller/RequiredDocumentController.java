package mx.edu.utez.sacit.controller;

import mx.edu.utez.sacit.dto.RequiredDocumentsDto;
import mx.edu.utez.sacit.service.RequiredDocumentService;
import mx.edu.utez.sacit.service.TransactionLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/required-documents")
public class RequiredDocumentController {

    private final RequiredDocumentService requiredDocumentService;
    private final TransactionLogService transactionLogService;

    public RequiredDocumentController(RequiredDocumentService requiredDocumentService, TransactionLogService transactionLogService) {
        this.requiredDocumentService = requiredDocumentService;
        this.transactionLogService = transactionLogService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getAll() {
        transactionLogService.logTransaction("OBTENER", "requiered_documents", "0", "Obtener_Documentos");
        return requiredDocumentService.findAll();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getOne(@PathVariable UUID uuid) {
        transactionLogService.logTransaction("OBTENER", "requiered_documents", uuid.toString(), "Obtener_Documento_UUID");
        return requiredDocumentService.findByUuid(uuid);
    }

    @GetMapping("/procedure/{procedureUuid}")
    public ResponseEntity<?> getByProcedure(@PathVariable UUID procedureUuid) {
        transactionLogService.logTransaction("OBTENER", "requiered_documents", "0", "Obtener_Documento_UUID_Procedure");
        return requiredDocumentService.findByProcedure(procedureUuid);
    }

    @PostMapping("/procedure/{procedureUuid}")
    public ResponseEntity<?> save(@RequestBody RequiredDocumentsDto documentDto, @PathVariable UUID procedureUuid) {
        transactionLogService.logTransaction("REGISTRAR", "requiered_documents", procedureUuid.toString(), "Documento_Procedure_Creado");
        return requiredDocumentService.save(documentDto, procedureUuid);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody RequiredDocumentsDto documentDto) {
        transactionLogService.logTransaction("ACTUALIZAR", "requiered_documents", uuid.toString(), "Documento_Actualizado");
        return requiredDocumentService.update(uuid, documentDto);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        transactionLogService.logTransaction("ELIMINAR", "requiered_documents", uuid.toString(), "Documento_Eliminado");
        return requiredDocumentService.delete(uuid);
    }
}
