package mx.edu.utez.SACIT.jwt;

import io.jsonwebtoken.*;
import mx.edu.utez.SACIT.model.UserModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {
    private static final long EXPIRE_DURATION = 24L * 60 * 60 * 1000;
    private static final Logger logger = LogManager.getLogger(JwtTokenUtil.class);
    @Value("${jwt.secret}")
    private String secretKey;

    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
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
                .setIssuer("SACIT")
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
