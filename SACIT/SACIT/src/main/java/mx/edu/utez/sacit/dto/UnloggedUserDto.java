package mx.edu.utez.sacit.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UnloggedUserDto {
    private String name;
    private String lastName;
    private String email;
}

