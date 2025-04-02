package mx.edu.utez.SACIT.repository;

import mx.edu.utez.SACIT.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    PasswordResetTokenRepository findByToken(String token);
}
