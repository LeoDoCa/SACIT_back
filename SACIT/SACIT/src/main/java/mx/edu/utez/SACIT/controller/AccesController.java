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

@RestController
@RequestMapping("api/")
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
            Authentication authentication = this.authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            logger.warn("{}", authentication);

            UserModel user = this.service.findByEmail(request.getEmail());
            String accessToken = this.jwtTokenUtil.generatedToken(user);
            String role = user.getRole().getRole();
            Integer id = user.getId();
            AuthResponse response = new AuthResponse(request.getEmail(), accessToken, role, id);
            return ResponseEntity.ok(response);
        }catch (BadCredentialsException e){
            Authentication authentication = this.authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            logger.warn("{}", authentication);
            return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
