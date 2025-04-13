package mx.edu.utez.sacit.controller;

import mx.edu.utez.sacit.jwt.AuthRequest;
import mx.edu.utez.sacit.jwt.AuthResponse;
import mx.edu.utez.sacit.jwt.JwtTokenUtil;
import mx.edu.utez.sacit.jwt.OtpRequest;
import mx.edu.utez.sacit.model.UserModel;
import mx.edu.utez.sacit.service.AccessLogService;
import mx.edu.utez.sacit.service.OtpService;
import mx.edu.utez.sacit.service.UserService;
import mx.edu.utez.sacit.service.email.EmailService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api")
public class AccesController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService service;
    private final OtpService otpService;
    private final EmailService emailService;
    private final AccessLogService accessLogService;
    private static  final Logger logger = LogManager.getLogger(AccesController.class);

    public AccesController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, UserService service, OtpService otpService, EmailService emailService, AccessLogService accessLogService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.service = service;
        this.otpService = otpService;
        this.emailService = emailService;
        this.accessLogService = accessLogService;
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, HttpServletRequest servletRequest) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    ));

            if (authentication.isAuthenticated()) {
                UserModel user = service.findByEmail(request.getEmail());
                String accessToken = jwtTokenUtil.generatedToken(user);
                String role = user.getRole().getRole();
                String name = user.getName();
                String lastname = user.getLastName();

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String username = request.getEmail();
                String ip = servletRequest.getRemoteAddr();
                String resource = "/api/login";
                accessLogService.registerEvent(username, "INICIO_SESION", ip, resource);

                servletRequest.getSession().setAttribute("authentication", authentication);

                logger.info("User {} logged in successfully", request.getEmail());
                return ResponseEntity.ok(new AuthResponse(name, lastname, request.getEmail(), accessToken, role));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (BadCredentialsException e) {
            String ipX = servletRequest.getRemoteAddr();
            accessLogService.registerEvent(request.getEmail(), "LOGIN_FALLIDO", ipX, "/api/login");
            logger.error("Login failed for user {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        token = token.substring(7);

        if (!jwtTokenUtil.validateAccessToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String subject = jwtTokenUtil.getSubject(token);
        String username = "ANÓNIMO";
        if (subject != null && subject.contains(",")) {
            String[] parts = subject.split(",");
            if (parts.length > 1) {
                username = parts[1].trim();
            }
        }
        if (username.equals("ANÓNIMO")) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                username = authentication.getName();
            }
        }

        String ip = request.getRemoteAddr();

        accessLogService.registerEvent(username, "CERRAR_SESION", ip, "/api/logout");
        logger.info("The user logs out successfully");
        return ResponseEntity.ok("Logout registrado exitosamente");
    }

    @PostMapping("/validate-credentials")
    public ResponseEntity<?> validateCredentials(@RequestBody AuthRequest request, HttpServletRequest servletRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            if (authentication.isAuthenticated()) {
                logger.info("Credenciales válidas para {}", request.getEmail());
                return ResponseEntity.ok("Credenciales válidas.");
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas.");
        } catch (BadCredentialsException e) {
            String ipX = servletRequest.getRemoteAddr();
            accessLogService.registerEvent(request.getEmail(), "LOGIN_FALLIDO", ipX, "/api/validate-credentials");
            logger.error("Credenciales inválidas para {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas.");
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody AuthRequest request) {
        UserModel user = service.findByEmail(request.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        String otp = otpService.generateOtp(request.getEmail());

        try {
            emailService.sendEmailWithOtp(request.getEmail(), otp);
            logger.info("OTP enviado exitosamente a {}", request.getEmail());
            return ResponseEntity.ok("OTP enviado.");
        } catch (Exception e) {
            logger.error("Error al enviar OTP: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar OTP.");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody OtpRequest request, HttpServletRequest servletRequest) {
        boolean isValidOtp = otpService.validateOtp(request.getEmail(), request.getOtp());
        Authentication authentication = (Authentication) servletRequest.getSession().getAttribute("authentication");
        if (isValidOtp) {
            UserModel user = service.findByEmail(request.getEmail());
            String accessToken = jwtTokenUtil.generatedToken(user);
            String role = user.getRole().getRole();
            String name = user.getName();
            String lastname = user.getLastName();


            SecurityContextHolder.getContext().setAuthentication(authentication);
            String username = request.getEmail();
            String ip = servletRequest.getRemoteAddr();
            String resource = "/api/verify-otp";
            accessLogService.registerEvent(username, "INICIO_SESION", ip, resource);

            servletRequest.getSession().setAttribute("userEmail", request.getEmail());

            logger.info("OTP verificado exitosamente para {}", request.getEmail());
            return ResponseEntity.ok(new AuthResponse(name, lastname, request.getEmail(), accessToken, role));
        } else {
            String ipX = servletRequest.getRemoteAddr();
            accessLogService.registerEvent(request.getEmail(), "LOGIN_FALLIDO", ipX, "/api/verify-otp");
            logger.error("OTP no válido para {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }
}
