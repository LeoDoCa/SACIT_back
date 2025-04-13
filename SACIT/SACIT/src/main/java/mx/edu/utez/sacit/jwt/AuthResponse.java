package mx.edu.utez.sacit.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
