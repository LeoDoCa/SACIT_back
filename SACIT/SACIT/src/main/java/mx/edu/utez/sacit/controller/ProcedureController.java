package mx.edu.utez.sacit.controller;

import mx.edu.utez.sacit.dto.ProceduresDto;
import mx.edu.utez.sacit.service.ProcedureService;
import mx.edu.utez.sacit.service.TransactionLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/procedures")
public class ProcedureController {

    private final ProcedureService procedureService;
    private final TransactionLogService transactionLogService;

    public ProcedureController(ProcedureService procedureService, TransactionLogService transactionLogService) {
        this.procedureService = procedureService;
        this.transactionLogService = transactionLogService;
    }

    @GetMapping("/")
    public ResponseEntity<?> getAll() {
        transactionLogService.logTransaction("OBTENER", "procedures", "0", "Obtener_Procedures");
        return procedureService.findAll();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getOne(@PathVariable UUID uuid) {
        transactionLogService.logTransaction("OBTENER", "procedures", uuid.toString(), "Obtener_Procedure_UUID");
        return procedureService.findByUuid(uuid);
    }

    @PostMapping("/")
    public ResponseEntity<?> save(@RequestBody ProceduresDto proceduresDto) {
        transactionLogService.logTransaction("REGISTRAR", "procedures", proceduresDto.getUuid().toString(), "Procedure_Creado");
        return procedureService.save(proceduresDto);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody ProceduresDto proceduresDto) {
        transactionLogService.logTransaction("ACTUALIZAR", "procedures", uuid.toString(), "Procedure_Actualizado");
        return procedureService.update(uuid, proceduresDto);
    }

    @PatchMapping("/{uuid}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID uuid, @RequestBody String status) {
        return procedureService.changeStatus(uuid, status);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        transactionLogService.logTransaction("ELIMINACION", "procedures", uuid.toString(), "Elimina_Procedures");
        return procedureService.delete(uuid);
    }
}