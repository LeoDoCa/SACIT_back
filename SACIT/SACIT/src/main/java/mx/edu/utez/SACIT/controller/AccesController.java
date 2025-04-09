package mx.edu.utez.SACIT.controller;

import mx.edu.utez.SACIT.jwt.AuthRequest;
import mx.edu.utez.SACIT.jwt.AuthResponse;
import mx.edu.utez.SACIT.jwt.JwtTokenUtil;
import mx.edu.utez.SACIT.model.UserModel;
import mx.edu.utez.SACIT.service.AccessLogService;
import mx.edu.utez.SACIT.service.UserService;
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

    private final AccessLogService accessLogService;
    private static  final Logger logger = LogManager.getLogger(AccesController.class);

    public AccesController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, UserService service, AccessLogService accessLogService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.service = service;
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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (authentication != null) ? authentication.getName() : "ANÓNIMO";
        String ip = request.getRemoteAddr();

        accessLogService.registerEvent(username, "CERRAR_SESION", ip, "/api/logout");
        logger.info("The user logs out successfully");
        return ResponseEntity.ok("Logout registrado exitosamente");
    }
}
