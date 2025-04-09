package mx.edu.utez.SACIT.controller;

import mx.edu.utez.SACIT.dto.WindowDTO;
import mx.edu.utez.SACIT.model.Window;

import mx.edu.utez.SACIT.service.WindowService;
import mx.edu.utez.SACIT.utils.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class WindowController {
    private final WindowService service;

    private static  final String RECORD_NOT_FOUND = "Record not found";
    private  static  final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final String SUCCESS_CODE = "200";
    private static final String NOTFOUND_CODE = "404";
    private static final String BADREQUEST_CODE = "400";
    private static final String INTERNAL_SERVER_ERROR_CODE = "500";
    private final Logger logger = LogManager.getLogger(WindowController.class);

    public WindowController( WindowService service) {
        this.service = service;

    }

    @GetMapping("/window")
    public List<Window> windows(){
      return  service.getAll();
    }

    @GetMapping("/window/{uuid}")
    public ResponseEntity<Object> getByUuid(@PathVariable UUID uuid) {
        return service.findByUuid(uuid)
            .map(window -> Utilities.generateResponse(HttpStatus.OK, "window found", SUCCESS_CODE))
            .orElseGet(() ->
                Utilities.generateResponse(HttpStatus.NOT_FOUND, RECORD_NOT_FOUND, NOTFOUND_CODE));
    }

    @PostMapping("/window/")
    public ResponseEntity save(@RequestBody WindowDTO window){
        try{
             if (window != null){

                 service.save(window);
                 return Utilities.generateResponse(HttpStatus.OK, "Record created successfully.",SUCCESS_CODE);
             }

             return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "All fields are required.", BADREQUEST_CODE);
        }catch (Exception e){

            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_CODE);
        }
    }

    @PutMapping("/window/{uuid}")
    public ResponseEntity<Object> update(@PathVariable UUID uuid,@RequestBody WindowDTO window){
        return service.findByUuid(uuid)
                .map(window1 -> {
                    window1.setWindowNumber(window.getWindowNumber());
                    window1.setStatus(window.getStatus());
                    window1.setUuid(window.getAttendantUuid());
                    service.update(window1);
                    return Utilities.generateResponse(HttpStatus.OK, "Record updated successfully.", SUCCESS_CODE);
                })
                .orElseGet(() ->
                        Utilities.generateResponse(HttpStatus.NOT_FOUND, RECORD_NOT_FOUND, NOTFOUND_CODE));
    }

    @DeleteMapping("/window/{uuid}")
    public ResponseEntity<Object> delete(@PathVariable UUID uuid){
        return service.findByUuid(uuid)
                .map(window -> {
                    service.delete(uuid);
                    return Utilities.generateResponse(HttpStatus.OK, "Record deleted successfully.", SUCCESS_CODE);
                })
                .orElseGet(() ->
                        Utilities.generateResponse(HttpStatus.NOT_FOUND, RECORD_NOT_FOUND, NOTFOUND_CODE));
    }

}
