package mx.edu.utez.SACIT.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WindowDTO {
    private String status;
    private Integer windowNumber;
    private LocalTime startTime;
    private LocalTime endTime;
    private UUID attendantUuid;

    public WindowDTO(String status, Integer windowNumber, LocalTime startTime, UUID attendantUuid, LocalTime endTime) {
        this.status = status;
        this.windowNumber = windowNumber;
        this.startTime = startTime;
        this.attendantUuid = attendantUuid;
        this.endTime = endTime;
    }
}


