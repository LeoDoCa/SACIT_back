package mx.edu.utez.SACIT.controller;

import mx.edu.utez.SACIT.dto.WindowDTO;
import mx.edu.utez.SACIT.model.Window;

import mx.edu.utez.SACIT.service.WindowService;
import mx.edu.utez.SACIT.utils.Utilities;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class WindowController {
    private final WindowService service;




    public WindowController(WindowService service) {
        this.service = service;

    }

    @GetMapping("/window")
    public List<Window> windows() {
        return service.getAll();
    }

    @GetMapping("/window/{uuid}")
    public ResponseEntity<?> getOne(@PathVariable UUID uuid) {
        return this.service.findByUuid(uuid);
    }

    @PostMapping("/window")
    public ResponseEntity save(@RequestBody WindowDTO window) {
        try {
            if (window != null) {

                service.save(window);
                return Utilities.generateResponse(HttpStatus.OK, "Record created successfully.", "200");
            }

            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "All fields are required.", "400");
        } catch (Exception e) {

            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }

    @PutMapping("/window/{uuid}")
    public ResponseEntity<?> update(@PathVariable UUID uuid, @RequestBody WindowDTO window) {
        if (window != null) {
            return service.changeStatus(window);

        }
        return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "All fields are required.", "400");
    }

    @DeleteMapping("/window/{uuid}")
    public ResponseEntity<?> delete(@PathVariable("uuid") UUID uuid) {
        service.delete(uuid);
        return Utilities.generateResponse(HttpStatus.OK, "Record deleted successfully.", "200");
    }

}
