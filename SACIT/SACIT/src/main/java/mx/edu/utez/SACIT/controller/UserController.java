package mx.edu.utez.SACIT.controller;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mx.edu.utez.SACIT.model.PasswordResetToken;
import mx.edu.utez.SACIT.repository.PasswordResetTokenRepository;
import mx.edu.utez.SACIT.service.email.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import mx.edu.utez.SACIT.model.RoleModel;
import mx.edu.utez.SACIT.model.UserModel;
import mx.edu.utez.SACIT.repository.RoleRepository;
import mx.edu.utez.SACIT.service.UserService;
import mx.edu.utez.SACIT.utils.Utilities;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final RoleRepository repository;
    private final PasswordResetTokenRepository passwordRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String RECORD_NOT_FOUND = "Record not found.";
    private static final String INTERNAL_SERVER_ERROR = "An internal server error occurred.";

    UserController(UserService userService, BCryptPasswordEncoder passwordEncoder, RoleRepository repository, EmailService emailService, PasswordResetTokenRepository passwordRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
        this.emailService = emailService;
        this.passwordRepository = passwordRepository;
    }

    @GetMapping("/user")
    public List<UserModel> users() {
        return this.userService.getAll();
    }

    @GetMapping("/user/{uuid}")
    public UserModel getByUuid(@PathVariable("uuid") UUID uuid) {
        return this.userService.findByUuid(uuid);
    }

    @PostMapping("/register")
    public ResponseEntity<Object> createUser(@RequestBody UserModel request) {
        try {
            String defaultRoleName = "ROLE_USER";
            RoleModel defaultRole = repository.findByRole(defaultRoleName)
                    .orElseThrow(() -> new RuntimeException("Default role not found"));

            request.setRole(defaultRole);
            request.setPassword(passwordEncoder.encode(request.getPassword()));
            this.userService.save(request);

            String title = "Bienvenido " + request.getName();
            String subject = "¡Te has registrado exitosamente en el sistema DOCES!";
            String message =
                    "<h2>¡Te damos la más cordial bienvenida al sistema SACIT!</h2>" +
                            "<p>Tu registro se ha completado exitosamente. Ahora podrás acceder a una plataforma donde gestionarás de manera " +
                            "eficiente los trámites de tu elección.</p>" +
                            "<p>Recuerda mantener tus credenciales seguras y no compartirlas con nadie.</p>" +
                            "<h3>Atentamente,</h3>" +
                            "<h3>El equipo de SACIT</h3>";
            emailService.sendSimpleEmail(request.getEmail(), title, subject, message);
            return Utilities.generateResponse(HttpStatus.OK, "Record created successfully.");
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/user/{uuid}")
    public ResponseEntity<Object> editUser(@PathVariable("uuid") UUID uuid, @RequestBody UserModel request) {
        try {
            UserModel user = this.getByUuid(uuid);
            if (user == null) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, RECORD_NOT_FOUND);
            } else {
                user.setName(request.getName());
                user.setLastName(request.getLastName());
                user.setEmail(request.getEmail());
                user.setRole(request.getRole());
                this.userService.save(user);
                return Utilities.generateResponse(HttpStatus.OK, "Record updated successfully.");
            }
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/recover-password-email")
    public ResponseEntity<Object> recoverPasswordEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            UserModel user = userService.findByEmail(email);
            if (user == null) {
                return Utilities.generateResponse(HttpStatus.NOT_FOUND, RECORD_NOT_FOUND);
            }
            PasswordResetToken existingToken = passwordRepository.findByUserAndExpiryDateAfter(user, LocalDateTime.now());
            if (existingToken != null) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Active password reset token already exists");
            }

            String token = UUID.randomUUID().toString();
            userService.savePasswordResetToken(user, token);

            String resetLink = "http://localhost:8012/reset-password/" + token;
            emailService.sendSimpleEmail(
                    email,
                    "Recupera tu contraseña",
                    "Recuperación de contraseña",
                    "Haz clic en el enlace para recuperar tu contraseña: <a href='" + resetLink + "'>Recuperar contraseña</a>"
            );
            return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.OK, "Token: " + token), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.BAD_REQUEST, INTERNAL_SERVER_ERROR), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Object> validateResetToken(@RequestParam String token) {
        PasswordResetToken resetToken = passwordRepository.findByToken(token);
        if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Token inválido o expirado"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.OK, "Token válido"), HttpStatus.OK);
    }

    @PostMapping("/reset-password/{token}")
    public ResponseEntity<Object> resetPassword(@PathVariable String token, @RequestBody Map<String, String> request) {
        try {
            String newPassword = request.get("password");
            PasswordResetToken resetToken = passwordRepository.findByToken(token);
            if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.BAD_REQUEST, INTERNAL_SERVER_ERROR),
                        HttpStatus.BAD_REQUEST);
            }
            UserModel user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            passwordRepository.delete(resetToken);
            return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.OK, "Password reset successfully"),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                    Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/user/{uuid}")
    public ResponseEntity<Object> deleteUser(@PathVariable("uuid") UUID uuid) {
        try {
            UserModel user = this.getByUuid(uuid);
            if (user == null) {
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, RECORD_NOT_FOUND);
            } else {
                this.userService.delete(uuid);
                return Utilities.generateResponse(HttpStatus.OK, "Record deleted successfully.");
            }
        } catch (Exception e) {
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, INTERNAL_SERVER_ERROR);
        }
    }
}
