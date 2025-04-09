package mx.edu.utez.SACIT.service.email;

import lombok.AllArgsConstructor;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import mx.edu.utez.SACIT.config.ApiResponse;
import mx.edu.utez.SACIT.repository.EmailRepository;
import mx.edu.utez.SACIT.utils.Utilities;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@AllArgsConstructor
public class EmailService implements EmailRepository {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    public static final String INTERNAL_SERVER_ERROR = "An internal server error occurred.";
    private static final String SUCCESS_CODE = "200";
    private static final String INTERNAL_SERVER_ERROR_CODE = "500";

    public ResponseEntity<ApiResponse> sendEmail(String toEmail, String subject, String title, String messageContent,
                                                 int type, MultipartFile file, String name) throws MessagingException {
        try {
            Context context = new Context();
            context.setVariable("title", title);
            context.setVariable("message", messageContent);
            context.setVariable("name", name);
            String[] plantillaAlerta = new String[] { "verificacion" };
            String htmlContent = templateEngine.process(plantillaAlerta[type], context);
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("sacit3mail@gmail.com");

            helper.addAttachment(file.getOriginalFilename(), file);
            context.setVariable("fileCid", "attachment-" + file.getOriginalFilename());
            javaMailSender.send(message);
            return new ResponseEntity<>(new ApiResponse(HttpStatus.OK, false, "El email se envio correctamente"),
                    HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse(HttpStatus.BAD_REQUEST, true, "No se envio el email"),
                    HttpStatus.OK);

        }

    }

    public void sendSimpleEmail(String toEmail, String title, String subject, String messageContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            String personalizedMessage = "<h1>" + title + "</h1>" + "<p>" + messageContent + "</p>";
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(personalizedMessage, true);
            helper.setFrom("sacit3mail@gmail.com");
            javaMailSender.send(message);
            new ResponseEntity<>(Utilities.generateResponse(HttpStatus.OK, "Correo enviado correctamente", SUCCESS_CODE),
                    HttpStatus.OK);
        } catch (Exception e) {
            new ResponseEntity<>(Utilities.generateResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_CODE),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
