package mx.edu.utez.SACIT.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequieredDocumentsDto {
    private Integer id;
    private UUID uuid;
    private String name;
    private String description;
    private Boolean mandatory;
}