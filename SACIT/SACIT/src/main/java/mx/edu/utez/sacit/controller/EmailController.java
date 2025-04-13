package mx.edu.utez.sacit.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.annotation.MultipartConfig;
import lombok.AllArgsConstructor;
import mx.edu.utez.sacit.config.ApiResponse;
import mx.edu.utez.sacit.service.email.EmailService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
@AllArgsConstructor
@MultipartConfig
public class EmailController {
    private EmailService emailService;
    private static final Logger logger = LogManager.getLogger(UserController.class);

    @PostMapping("/sendEmail")
    public ResponseEntity<ApiResponse> senderEmail(@RequestParam("toEmail") String email,
                                                   @RequestParam("subject") String subject,
                                                   @RequestParam("title") String title,
                                                   @RequestParam("messageContent") String messageContent,
                                                   @RequestParam("type") int type,
                                                   @RequestParam("file") MultipartFile file,
                                                   @RequestParam("name") String name) throws MessagingException {
        logger.info("Envío de correo electrónico al email: {}", email);
        return emailService
                .sendEmail(email, subject, title, messageContent, type, file, name);
    }
}
