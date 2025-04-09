package mx.edu.utez.SACIT.dto;

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
    private String phone;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate creationDate;
    private String confirmationCode;
    private String cancellationReason;
    private String status;
}