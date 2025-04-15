package mx.edu.utez.sacit.repository;

import mx.edu.utez.sacit.model.PasswordResetToken;
import mx.edu.utez.sacit.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByToken(String token);
    PasswordResetToken findByUserAndExpiryDateAfter(UserModel user, LocalDateTime now);

}
