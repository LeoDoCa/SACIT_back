package mx.edu.utez.SACIT.controller;

import mx.edu.utez.SACIT.dto.RequiredDocumentsDto;
import mx.edu.utez.SACIT.service.RequiredDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/required-documents")
public class RequiredDocumentController {

    @Autowired
    private RequiredDocumentService requiredDocumentService;

    @GetMapping("/")
    public ResponseEntity<?> getAll() {
        return requiredDocumentService.findAll();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getOne(@PathVariable UUID uuid) {
        return requiredDocumentService.findByUuid(uuid);
    }

    @GetMapping("/procedure/{procedureUuid}")
    public ResponseEntity<?> getByProcedure(@PathVariable UUID procedureUuid) {
        return requiredDocumentService.findByProcedure(procedureUuid);
    }

    @PostMapping("/procedure/{procedureUuid}")
    public ResponseEntity<?> save(@RequestBody RequiredDocumentsDto documentDto, @PathVariable UUID procedureUuid) {
        return requiredDocumentService.save(documentDto, procedureUuid);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody RequiredDocumentsDto documentDto) {
        return requiredDocumentService.update(uuid, documentDto);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        return requiredDocumentService.delete(uuid);
    }
}
