package mx.edu.utez.sacit.controller;

import mx.edu.utez.sacit.model.RoleModel;
import mx.edu.utez.sacit.service.RoleService;
import mx.edu.utez.sacit.service.TransactionLogService;
import mx.edu.utez.sacit.utils.Utilities;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RoleController {

    private final RoleService roleService;
    private final TransactionLogService transactionLogService;
    private static final Logger logger = LogManager.getLogger(RoleController.class);
    private static final String SUCCESS_CODE = "200";

    public RoleController(RoleService roleService, TransactionLogService transactionLogService) {
        this.roleService = roleService;
        this.transactionLogService = transactionLogService;
    }

    @GetMapping("/role")
    public List<RoleModel> roles() {
        logger.info("Solicitud para obtener todos los roles");
        transactionLogService.logTransaction("OBTENER", "roles", "0", "Obtener_Roles");
        return roleService.getAll();
    }

    @GetMapping("/role/{uuid}")
    public ResponseEntity getByUuid(@PathVariable UUID uuid){
        logger.info("Solicitud para obtener un rol por su UUID: {}", uuid);
        transactionLogService.logTransaction("OBTENER", "roles", uuid.toString(), "Obtener_Rol_UUID");
        return roleService.findByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping("/role")
    public ResponseEntity save(@RequestBody RoleModel role){
        this.roleService.saveRole(role);
        logger.info("Solicitud para registrar un nuevo rol: {}", role);
        transactionLogService.logTransaction("REGISTRAR", "roles", role.getUuid().toString(), "Rol_Creado");
        return Utilities.generateResponse(HttpStatus.OK, "Record created successfully.",SUCCESS_CODE);
    }

    @PutMapping("/role/{uuid}")
    public ResponseEntity<RoleModel> update (@PathVariable UUID uuid, @RequestBody RoleModel role){
        logger.info("Solicitud para actualizar un rol: {}", role);
        transactionLogService.logTransaction("REGISTRAR", "roles", role.getUuid().toString(), "Rol_Creado");
        return roleService.findByUuid(uuid)
                .map(roleObj ->{
                    roleObj.setRole(role.getRole());
                    return ResponseEntity.ok(roleService.saveRole(roleObj));
                })
                .orElseGet(()->ResponseEntity.notFound().build());
    }

    @DeleteMapping("/role/{uuid}")
    public ResponseEntity<RoleModel> delete(@PathVariable UUID uuid){
        logger.warn("Solicitud para eliminar un rol por su UUID: {}", uuid);
        transactionLogService.logTransaction("ELIMINACION", "roles", uuid.toString(), "Rol_Eliminado");
        return roleService. findByUuid(uuid)
                .map(role -> {
                    roleService.delete(uuid);
                    return ResponseEntity.ok(role);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
