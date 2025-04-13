package mx.edu.utez.sacit.controller;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mx.edu.utez.sacit.model.PasswordResetToken;
import mx.edu.utez.sacit.repository.PasswordResetTokenRepository;
import mx.edu.utez.sacit.service.email.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import mx.edu.utez.sacit.model.RoleModel;
import mx.edu.utez.sacit.model.UserModel;
import mx.edu.utez.sacit.repository.RoleRepository;
import mx.edu.utez.sacit.service.UserService;
import mx.edu.utez.sacit.service.TransactionLogService;
import mx.edu.utez.sacit.utils.Utilities;
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
    private final TransactionLogService transactionLogService;
    private static final Logger logger = LogManager.getLogger(UserController.class);


    UserController(UserService userService, BCryptPasswordEncoder passwordEncoder, RoleRepository repository, EmailService emailService, PasswordResetTokenRepository passwordRepository, TransactionLogService transactionLogService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.repository = repository;
        this.emailService = emailService;
        this.passwordRepository = passwordRepository;
        this.transactionLogService = transactionLogService;
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
                return Utilities.generateResponse(HttpStatus.NOT_FOUND, "No se encontraron usuarios con el rol proporcionado", "404");
            }
            logger.info("Usuarios encontrados con el rol {}: {}", roleName, users.size());
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error al consultar usuarios por rol: {}", roleName, e);
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error al consultar usuarios por rol", "500");
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
                        "El correo electrónico ya está registrado. Por favor utilice otro.", "409");
            }

            RoleModel role;
            if (request.getRole() != null && request.getRole().getRole() != null) {
                role = repository.findByRole(request.getRole().getRole())
                        .orElseThrow(() -> new RuntimeException("El rol proporcionado no existe"));
            } else {
                String defaultRoleName = "ROLE_USER";
                role = repository.findByRole(defaultRoleName)
                        .orElseThrow(() -> new RuntimeException("Default role not found"));
            }

            request.setRole(role);
            request.setPassword(passwordEncoder.encode(request.getPassword()));
            this.userService.save(request);
            logger.info("Usuario registrado exitosamente: {}", request.getEmail());

            String title = "Bienvenido " + request.getName();
            String subject = "¡Te has registrado exitosamente en el sistema sacit!";
            String message = "<h2>¡Te damos la más cordial bienvenida al sistema sacit!</h2>" +
                    "<p>Tu registro se ha completado exitosamente. Ahora podrás acceder a una plataforma donde gestionarás de manera "
                    +
                    "eficiente los trámites de tu elección.</p>" +
                    "<p>Recuerda mantener tus credenciales seguras y no compartirlas con nadie.</p>" +
                    "<h3>Atentamente,</h3>" +
                    "<h3>El equipo de sacit</h3>";
            emailService.sendSimpleEmail(request.getEmail(), title, subject, message);
            transactionLogService.logTransaction("REGISTRO", "users", request.getId(), "Usuario_Registrado");
            return Utilities.generateResponse(HttpStatus.OK, "Record created successfully.", "200");
        } catch (Exception e) {
            logger.error("Error al registrar usuario: {}", request.getEmail(), e);
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An Internal Server error occurred", "500");
        }
    }

    @PutMapping("/user/{uuid}")
    public ResponseEntity<Object> editUser(@PathVariable("uuid") UUID uuid, @RequestBody Map<String, String> request) {
        logger.info("Solicitud de actualización para usuario con UUID: {}", uuid);
        try {
            UserModel user = this.getByUuid(uuid);
            if (user == null) {
                logger.warn("Usuario no encontrado con UUID: {}", uuid);
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST,"User not found", "404");
            } else {
                if (request.containsKey("name")) {
                    user.setName(request.get("name"));
                }

                if (request.containsKey("lastName")) {
                    user.setLastName(request.get("lastName"));
                }

                if (request.containsKey("password") && !request.get("password").isEmpty()) {
                    String encodedPassword = passwordEncoder.encode(request.get("password"));
                    user.setPassword(encodedPassword);
                }

                this.userService.save(user);
                logger.info("Usuario actualizado correctamente: {}", uuid);
                transactionLogService.logTransaction("ACTUALIZACION", "users", user.getId(), "Usuario_Actualizado");
                return Utilities.generateResponse(HttpStatus.OK, "Record updated successfully.", "200");
            }
        } catch (Exception e) {
            logger.error("Error al actualizar usuario con UUID: {}", uuid, e);
            return Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,"An internal server error occurred", "500");
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
                return Utilities.generateResponse(HttpStatus.NOT_FOUND, "User not found", "404");
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
            transactionLogService.logTransaction("RECUPERACION_CONTRASENA", "users", user.getId(), "Token_Generado");
            return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.OK, "Token: " + token, "200"), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error durante la recuperación de contraseña para el correo {}: {}", email, e.getMessage(), e);
            return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.BAD_REQUEST, "An internal server error occurred","500"), HttpStatus.BAD_REQUEST);
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
                return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.BAD_REQUEST, "An internal server error  occurred","500"),
                        HttpStatus.BAD_REQUEST);
            }
            UserModel user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            passwordRepository.delete(resetToken);
            logger.info("Contraseña restablecida exitosamente para el usuario: {}", user.getEmail());
            transactionLogService.logTransaction("CAMBIO_CONTRASENA", "users", user.getId(), "Contraseña_Cambiada");
            return new ResponseEntity<>(Utilities.generateResponse(HttpStatus.OK, "Password reset successfully","200"),
                    HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error al restablecer contraseña con token {}: {}", token, e.getMessage(), e);
            return new ResponseEntity<>(
                    Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR,"An Internal server error occurred","500"),
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
                return Utilities.generateResponse(HttpStatus.BAD_REQUEST,"User not found", "404");
            } else {
                this.userService.delete(uuid);
                logger.info("Usuario eliminado correctamente: {}", uuid);
                transactionLogService.logTransaction("ELIMINACION", "users", user.getId(), "Usuario_Eliminado");
                return Utilities.generateResponse(HttpStatus.OK, "Record deleted successfully.", "200");
            }
        } catch (Exception e) {
            logger.error("Error al eliminar usuario con UUID: {}", uuid, e);
            return Utilities.generateResponse(HttpStatus.BAD_REQUEST,"An internal server error occurred", "500");
        }
    }
}
