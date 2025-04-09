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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final RoleRepository repository;
    private final PasswordResetTokenRepository passwordRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private static final Logger logger = LogManager.getLogger(UserController.class);

    private static final String RECORD_NOT_FOUND = "Record not found.";
    private static final String INTERNAL_SERVER_ERROR = "An internal server error occurred.";
    private static final String NOTFOUND_CODE = "404";
    private static final String INTERNAL_SERVER_ERROR_CODE = "500";

    UserController(UserService userService, BCryptPasswordEncoder passwordEncoder, RoleRepository repository, EmailService emailService, PasswordResetTokenRepository passwordRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
        this.emailService = emailService;
        this.passwordRepository = passwordRepository;
    }

    @GetMapping("/user")
    public List<UserModel> users() {
        logger.info("Solicitud para obtener todos los usuarios");
        return this.userService.getAll();
    }

    @GetMapping("/user/{roleName}")
    public ResponseEntity<Object> getUsersByRole(@PathVariable("roleName") String roleName) {
        logger.info("Solicitud para obtener usuarios con rol: {}", roleName);
        try {
            List<UserModel> users = userService.getUsersByRoleName(roleName.toUpperCase());
            if (users.isEmpty()) {
                logger.warn("No se encontraron usuarios con el rol: {}", roleName);
                return Utilities.generateResponse(HttpStatus.NOT_FOUND, "No se encontraron usuarios con el rol proporcionado", NOTFOUND_CODE);
            }
            logger.info("Usuarios encontrados con el rol {}: {}", roleName, users.size());
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error al consultar usuarios por rol: {}", roleName, e);
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error al consultar usuarios por rol", INTERNAL_SERVER_ERROR_CODE);
        }
    }

    @GetMapping("/user/{uuid}")
    public UserModel getByUuid(@PathVariable("uuid") UUID uuid) {
        logger.info("Solicitud para obtener usuario con UUID: {}", uuid);
        return this.userService.findByUuid(uuid);
    }

    @PostMapping("/register")
    public ResponseEntity<Object> createUser(@RequestBody UserModel request) {
        logger.info("Solicitud de registro para usuario: {}", request.getEmail());
        try {
            UserModel existingUser = userService.findByEmail(request.getEmail());
            if (existingUser != null) {
                logger.warn("Intento de registro con correo ya existente: {}", request.getEmail());
                return Utilities.generateResponse(
                        HttpStatus.CONFLICT,
                        "El correo electrónico ya está registrado. Por favor utilice otro.","409"
                );
            }

            String defaultRoleName = "ROLE_USER";
            RoleModel defaultRole = repository.findByRole(defaultRoleName)
                    .orElseThrow(() -> new RuntimeException("Default role not found"));

            request.setRole(defaultRole);
            request.setPassword(passwordEncoder.encode(request.getPassword()));
            this.userService.save(request);
            logger.info("Usuario registrado exitosamente: {}", request.getEmail());
            String title = "Bienvenido " + request.getName();
            String subject = "¡Te has registrado exitosamente en el sistema SACIT!";
            String message =
                    "<h2>¡Te damos la más cordial bienvenida al sistema SACIT!</h2>" +
                            "<p>Tu registro se ha completado exitosamente. Ahora podrás acceder a una plataforma donde gestionarás de manera " +
                            "eficiente los trámites de tu elección.</p>" +
                            "<p>Recuerda mantener tus credenciales seguras y no compartirlas con nadie.</p>" +
                            "<h3>Atentamente,</h3>" +
                            "<h3>El equipo de SACIT</h3>";
            emailService.sendSimpleEmail(request.getEmail(), title, subject, message);
            return Utilities.generateResponse(HttpStatus.OK, "Record created successfully.","200");
        } catch (Exception e) {
            logger.error("Error al registrar usuario: {}", request.getEmail(), e);
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR,"500");
        }
    }

    @PutMapping("/user/{uuid}")
    public ResponseEntity<Object> editUser(@PathVariable("uuid") UUID uuid, @RequestBody UserModel request) {
        logger.info("Solicitud de actualización para usuario con UUID: {}", uuid);
        try {
            UserModel user = this.getByUuid(uuid);
            if (user == null) {
                logger.warn("Usuario no encontrado con UUID: {}", uuid);
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, RECORD_NOT_FOUND,"404");
            } else {
                user.setName(request.getName());
                user.setLastName(request.getLastName());
                user.setEmail(request.getEmail());
                user.setRole(request.getRole());
                this.userService.save(user);
                logger.info("Usuario actualizado correctamente: {}", uuid);
                return Utilities.generateResponse(HttpStatus.OK, "Record updated successfully.","200");
            }
        } catch (Exception e) {
            logger.error("Error al actualizar usuario con UUID: {}", uuid, e);
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, INTERNAL_SERVER_ERROR,"500");
        }
    }

    @PostMapping("/recover-password-email")
    public ResponseEntity<Object> recoverPasswordEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("Solicitud de recuperación de contraseña para el correo: {}", email);
        try {
            UserModel user = userService.findByEmail(email);
            if (user == null) {
                logger.warn("No se encontró un usuario con el correo: {}", email);
                return Utilities.generateResponse(HttpStatus.NOT_FOUND, RECORD_NOT_FOUND, "404");
            }
            PasswordResetToken existingToken = passwordRepository.findByUserAndExpiryDateAfter(user, LocalDateTime.now());
            if (existingToken != null) {
                logger.warn("Ya existe un token activo para el usuario: {}", email);
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Active password reset token already exists", "400");
            }

            String token = UUID.randomUUID().toString();
            userService.savePasswordResetToken(user, token);
            logger.info("Token generado correctamente para el usuario: {}", email);

            String resetLink = "http://localhost:5173/reset-password/" + token;
            emailService.sendSimpleEmail(
                    email,
                    "Recupera tu contraseña",
                    "Recuperación de contraseña",
                    "Haz clic en el enlace para recuperar tu contraseña: <a href='" + resetLink + "'>Recuperar contraseña</a>"
            );
            logger.info("Correo de recuperación enviado a: {}", email);
            return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.OK, "Token: " + token, "200"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error durante la recuperación de contraseña para el correo {}: {}", email, e.getMessage(), e);
            return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.BAD_REQUEST, INTERNAL_SERVER_ERROR,"500"), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Object> validateResetToken(@RequestParam String token) {
        logger.info("Validando token de recuperación: {}", token);
        PasswordResetToken resetToken = passwordRepository.findByToken(token);

        if (resetToken == null || resetToken.getExpiryDate() == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            logger.warn("Token inválido o expirado: {}", token);
            if (resetToken != null) passwordRepository.delete(resetToken);
            return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.BAD_REQUEST, "Token inválido o expirado","400"), HttpStatus.BAD_REQUEST);
        }
        logger.info("Token válido: {}", token);
        return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.OK, "Token válido","200"), HttpStatus.OK);
    }

    @PostMapping("/reset-password/{token}")
    public ResponseEntity<Object> resetPassword(@PathVariable String token, @RequestBody Map<String, String> request) {
        logger.info("Solicitud para restablecer contraseña con token: {}", token);
        try {
            String newPassword = request.get("password");
            PasswordResetToken resetToken = passwordRepository.findByToken(token);
            if (resetToken == null || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                logger.warn("Token inválido o expirado al intentar restablecer contraseña: {}", token);
                return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.BAD_REQUEST, INTERNAL_SERVER_ERROR,"500"),
                        HttpStatus.BAD_REQUEST);
            }
            UserModel user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            passwordRepository.delete(resetToken);
            logger.info("Contraseña restablecida exitosamente para el usuario: {}", user.getEmail());
            return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.OK, "Password reset successfully","200"),
                    HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error al restablecer contraseña con token {}: {}", token, e.getMessage(), e);
            return new ResponseEntity<>(
                    Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR,"500"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/user/{uuid}")
    public ResponseEntity<Object> deleteUser(@PathVariable("uuid") UUID uuid) {
        logger.info("Solicitud para eliminar usuario con UUID: {}", uuid);
        try {
            UserModel user = this.getByUuid(uuid);
            if (user == null) {
                logger.warn("Usuario no encontrado para eliminar: {}", uuid);
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST, RECORD_NOT_FOUND, "404");
            } else {
                this.userService.delete(uuid);
                logger.info("Usuario eliminado correctamente: {}", uuid);
                return Utilities.generateResponse(HttpStatus.OK, "Record deleted successfully.", "200");
            }
        } catch (Exception e) {
            logger.error("Error al eliminar usuario con UUID: {}", uuid, e);
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST, INTERNAL_SERVER_ERROR, "500");
        }
    }
}
