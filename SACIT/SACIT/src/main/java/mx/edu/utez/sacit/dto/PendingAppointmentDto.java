package mx.edu.utez.sacit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingAppointmentDto {
    private Integer id;
    private UUID uuid;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate creationDate;
    private String status;

    private UUID userUuid;
    private String userName;
    private String userLastName;
    private String userEmail;

    private UUID procedureUuid;
    private String procedureName;

    private UUID windowUuid;
    private Integer windowNumber;

}
