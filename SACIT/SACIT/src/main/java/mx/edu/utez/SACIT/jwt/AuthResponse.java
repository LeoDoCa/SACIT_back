package mx.edu.utez.SACIT.jwt;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String email;
    private String accessToken;
    private String role;
    private UUID uuid;
}
