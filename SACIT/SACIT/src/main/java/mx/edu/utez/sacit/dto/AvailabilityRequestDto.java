package mx.edu.utez.sacit.dto;

import java.time.LocalDate;
import java.util.UUID;

public class AvailabilityRequestDto {
    private LocalDate date;
    private UUID procedureUuid;

    public AvailabilityRequestDto() {
    }

    public AvailabilityRequestDto(LocalDate date, UUID procedureUuid) {
        this.date = date;
        this.procedureUuid = procedureUuid;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public UUID getProcedureUuid() {
        return procedureUuid;
    }

    public void setProcedureUuid(UUID procedureUuid) {
        this.procedureUuid = procedureUuid;
    }
}
