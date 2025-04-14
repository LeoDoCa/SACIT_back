package mx.edu.utez.sacit.jwt;

import io.jsonwebtoken.*;
import mx.edu.utez.sacit.model.UserModel;
import mx.edu.utez.sacit.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {
    private static final long EXPIRE_DURATION = 60L * 60 * 1000;
    private final UserRepository userRepository;
    private static final Logger logger = LogManager.getLogger(JwtTokenUtil.class);
    @Value("${jwt.secret}")
    private String secretKey;

    public JwtTokenUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            String subject = claims.getSubject();
            String[] parts = subject.split(",\\s*");
            if (parts.length != 2) {
                logger.error("Invalid token subject format");
                return false;
            }

            String email = parts[1];

            UserModel user = userRepository.findByEmail(email);
            if (user == null) {
                logger.error("User from token no longer exists in the database");
                return false;
            }

            return true;

        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("Token is null, empty, or contains errors {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT", ex);
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT", ex);
        } catch (JwtException ex) {
            logger.error("Signature validation failed");
        }
        return false;
    }

    public String generatedToken(UserModel usuario){
        return Jwts.builder()
                .setSubject(String.format("%s, %s", usuario.getId(), usuario.getEmail()))
                .claim("uuid", usuario.getUuid().toString())
                .setIssuer("sacit")
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + EXPIRE_DURATION))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    public String getSubject(String token){
        return parseClaims(token).getSubject();
    }

    private Claims parseClaims(String token){
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }
}
