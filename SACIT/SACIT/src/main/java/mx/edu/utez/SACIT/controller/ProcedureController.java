package mx.edu.utez.SACIT.controller;

import mx.edu.utez.SACIT.dto.ProceduresDto;
import mx.edu.utez.SACIT.service.ProcedureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/procedures")
public class ProcedureController {

    @Autowired
    private ProcedureService procedureService;

    @GetMapping("/")
    public ResponseEntity<?> getAll() {
        return procedureService.findAll();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getOne(@PathVariable UUID uuid) {
        return procedureService.findByUuid(uuid);
    }

    @PostMapping("/")
    public ResponseEntity<?> save(@RequestBody ProceduresDto proceduresDto) {
        return procedureService.save(proceduresDto);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody ProceduresDto proceduresDto) {
        return procedureService.update(uuid, proceduresDto);
    }

    @PatchMapping("/{uuid}/status")
    public ResponseEntity<?> updateStatus(@PathVariable UUID uuid, @RequestBody String status) {
        return procedureService.changeStatus(uuid, status);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        return procedureService.delete(uuid);
    }
}