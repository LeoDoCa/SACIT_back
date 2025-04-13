package mx.edu.utez.sacit.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class AppointmentDto {
    private Integer id;
    private UUID uuid;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate creationDate;
    private String status;
}