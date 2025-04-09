package mx.edu.utez.SACIT.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ProceduresDto {
    private Integer creatorId;
    private UUID uuid;
    private String name;
    private String description;
    private Double cost;
    private Integer stimatedTime;
    private String creationDate;
    private String status;
}