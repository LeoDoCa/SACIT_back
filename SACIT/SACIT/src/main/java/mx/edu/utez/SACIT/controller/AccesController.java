package mx.edu.utez.SACIT.controller;

import mx.edu.utez.SACIT.jwt.AuthRequest;
import mx.edu.utez.SACIT.jwt.AuthResponse;
import mx.edu.utez.SACIT.jwt.JwtTokenUtil;
import mx.edu.utez.SACIT.model.UserModel;
import mx.edu.utez.SACIT.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AccesController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService service;

    private static  final Logger logger = LogManager.getLogger(AccesController.class);

    public AccesController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, UserService service) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.service = service;
    }
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
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

            logger.info("User {} logged in successfully", request.getEmail());
            return ResponseEntity.ok(new AuthResponse(name, lastname, request.getEmail(), accessToken, role));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (BadCredentialsException e) {
        logger.error("Login failed for user {}: {}", request.getEmail(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
}
