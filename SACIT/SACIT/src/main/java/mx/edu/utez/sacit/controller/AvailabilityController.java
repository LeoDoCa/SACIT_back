package mx.edu.utez.sacit.controller;

import mx.edu.utez.sacit.dto.AvailabilityRequestDto;
import mx.edu.utez.sacit.service.AvailabilityService;
import mx.edu.utez.sacit.utils.Utilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private static final Logger logger = LogManager.getLogger(AvailabilityController.class);

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/available-times")
    public ResponseEntity<?> getAvailableTimesPost(@RequestBody AvailabilityRequestDto requestDto) {
        try {
            logger.info("Inicio de consulta de horarios disponibles para la fecha: {}, trámite UUID: {}",
                    requestDto.getDate(), requestDto.getProcedureUuid());

            Map<String, Object> availabilityData = availabilityService
                    .getAvailableTimes(requestDto.getDate(), requestDto.getProcedureUuid());

            logger.info("Horarios disponibles obtenidos exitosamente para la fecha: {}, trámite UUID: {}",
                    requestDto.getDate(), requestDto.getProcedureUuid());
            return Utilities.ResponseWithData(HttpStatus.OK, "Available times retrieved successfully", "200", availabilityData);
        } catch (IllegalArgumentException e) {
            logger.warn("Argumento inválido al obtener horarios disponibles: {}", e.getMessage());
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, e.getMessage(), "400");
        } catch (Exception e) {
            logger.error("Error interno al obtener horarios disponibles: {}", e.getMessage(), e);
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving available times: " + e.getMessage(), "500");
        }
    }

    @PostMapping("/")
    public ResponseEntity<?> getAllAvailableTimes(@RequestBody AvailabilityRequestDto requestDto) {
        try {
            logger.info("Inicio de consulta de todos los horarios disponibles para la fecha: {}, trámite UUID: {}",
                    requestDto.getDate(), requestDto.getProcedureUuid());

            List<String> availableTimes = availabilityService.getAllAvailableTimes(
                    requestDto.getDate(),
                    requestDto.getProcedureUuid()
            );

            logger.info("Todos los horarios disponibles obtenidos exitosamente para la fecha: {}, trámite UUID: {}",
                    requestDto.getDate(), requestDto.getProcedureUuid());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("date", requestDto.getDate().toString());
            responseData.put("procedureUuid", requestDto.getProcedureUuid());
            responseData.put("availableTimes", availableTimes);

            return Utilities.ResponseWithData(HttpStatus.OK,
                    "Available times retrieved successfully",
                    "200",
                    responseData);

        } catch (IllegalArgumentException e) {
            logger.warn("Argumento inválido al obtener todos los horarios disponibles: {}", e.getMessage());
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, e.getMessage(), "400");
        } catch (Exception e) {
            logger.error("Error interno al obtener todos los horarios disponibles: {}", e.getMessage(), e);
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving available times: " + e.getMessage(),
                    "500");
        }
    }
}
