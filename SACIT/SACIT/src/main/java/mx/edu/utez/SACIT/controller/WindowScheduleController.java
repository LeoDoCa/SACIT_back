package mx.edu.utez.SACIT.controller;

import mx.edu.utez.SACIT.dto.WindowScheduleDTO;
import mx.edu.utez.SACIT.model.WindowSchedule;
import mx.edu.utez.SACIT.service.WindowsScheduleService;
import mx.edu.utez.SACIT.utils.Utilities;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class WindowScheduleController {

    private final WindowsScheduleService windowsScheduleService;

    public WindowScheduleController(WindowsScheduleService windowsScheduleService) {
        this.windowsScheduleService = windowsScheduleService;
    }

    @GetMapping("/windowsschedule")
    public List<WindowSchedule> windowSchedules(){
        return windowsScheduleService.getAll();
    }

    @GetMapping("/windowsschedule/{uuid}")
    public ResponseEntity<Object> getByUuid(@PathVariable UUID uuid){
        return windowsScheduleService.findByUuuid(uuid)
                .map(shedule -> Utilities.generateResponse(HttpStatus.OK,"Window schedule found", "200"))
                .orElseGet(() -> Utilities.generateResponse(HttpStatus.NOT_FOUND,"Record not found", "404"));
    }

    @PostMapping("/windowsschedule")
    public ResponseEntity<Object> save(@RequestBody WindowScheduleDTO dto){
        try {
            if (dto != null) {
                windowsScheduleService.saved(dto);
                return Utilities.generateResponse(HttpStatus.OK, "Record created successfully.", "200");
            }
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "All fields are required.", "400");
        }catch (IllegalArgumentException e){
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, e.getMessage(), "400");
        }catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }


    @DeleteMapping("/windowsschedule/{uuid}")
    public ResponseEntity<Object> delete(@PathVariable UUID uuid){
        try {
            return windowsScheduleService.findByUuuid(uuid)
                    .map(windowSchedule -> {
                        windowsScheduleService.delete(uuid);
                        return Utilities.generateResponse(HttpStatus.OK, "Record deleted successfully.", "200");
                    })
                    .orElseGet(() ->
                            Utilities.generateResponse(HttpStatus.NOT_FOUND, "Record not found", "404"));
        }catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "500");
        }
    }
}
