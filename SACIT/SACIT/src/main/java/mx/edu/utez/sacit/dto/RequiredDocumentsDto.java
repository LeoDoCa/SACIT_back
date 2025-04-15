package mx.edu.utez.sacit.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequiredDocumentsDto {
    private Integer id;
    private UUID uuid;
    private String name;
}
