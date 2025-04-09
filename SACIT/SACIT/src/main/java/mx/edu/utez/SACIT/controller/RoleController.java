package mx.edu.utez.SACIT.controller;

import mx.edu.utez.SACIT.model.RoleModel;
import mx.edu.utez.SACIT.service.RoleService;
import mx.edu.utez.SACIT.utils.Utilities;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class RoleController {

    private final RoleService roleService;
    private static final String RECORD_NOT_FOUND = "Record not found.";
    private static final String INTERNAL_SERVER_ERROR = "An internal server error occurred.";

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/role")
    public List<RoleModel> roles() {
        return roleService.getAll();
    }

    @GetMapping("/role/{uuid}")
    public ResponseEntity getByUuid(@PathVariable UUID uuid){
        return roleService.findByUuid(uuid)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping("/role")
    public ResponseEntity save(@RequestBody RoleModel role){
        this.roleService.saveRole(role);
        return Utilities.generateResponse(HttpStatus.OK, "Record created successfully.","200");
    }

    @PutMapping("/role/{uuid}")
    public ResponseEntity<RoleModel> update (@PathVariable UUID uuid, @RequestBody RoleModel role){
        return roleService.findByUuid(uuid)
                .map(roleObj ->{
                    roleObj.setRole(role.getRole());
                    return ResponseEntity.ok(roleService.saveRole(roleObj));
                })
                .orElseGet(()->ResponseEntity.notFound().build());
    }

    @DeleteMapping("/role/{uuid}")
    public ResponseEntity<RoleModel> delete(@PathVariable UUID uuid){
        return roleService. findByUuid(uuid)
                .map(role -> {
                    roleService.delete(uuid);
                    return ResponseEntity.ok(role);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
