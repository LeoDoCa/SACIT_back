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
    public ResponseEntity<AuthResponse> loging(@RequestBody AuthRequest request){
        try{
            UserModel user = this.service.findByEmail(request.getEmail());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse("No user registered with this email", null, null, null));
            }
            Authentication authentication = this.authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
            logger.warn("{}", authentication);
            String accessToken = this.jwtTokenUtil.generatedToken(user);
            String role = user.getRole().getRole();
            UUID uuid = user.getUuid();
            AuthResponse response = new AuthResponse(request.getEmail(), accessToken, role, uuid);
            return ResponseEntity.ok(response);
        }catch (BadCredentialsException e){
            Authentication authentication = this.authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            logger.warn("{}", authentication);
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse("An error occurred", null, null, null));
        }
    }
}
