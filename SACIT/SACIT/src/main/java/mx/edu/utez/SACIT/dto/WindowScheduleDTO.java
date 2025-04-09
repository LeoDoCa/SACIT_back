package mx.edu.utez.SACIT.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.edu.utez.SACIT.model.WindowSchedule;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WindowScheduleDTO {
    private Integer dayWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private UUID windowUuid;
}
