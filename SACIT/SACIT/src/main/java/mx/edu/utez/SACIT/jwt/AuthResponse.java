package mx.edu.utez.SACIT.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String name;
    private String lastName;
    private String email;
    private String accessToken;
    private String role;

}
