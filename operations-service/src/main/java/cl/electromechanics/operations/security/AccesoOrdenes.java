package cl.electromechanics.operations.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class AccesoOrdenes {
    private final SecretKeySpec clave;

    public AccesoOrdenes(@Value("${jwt.secret}") String secreto) {
        clave = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public Long exigirRol(String authorization, String rol) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Se requiere iniciar sesión");
        }

        Claims claims;
        Long id;
        try {
            claims = Jwts.parser().verifyWith(clave).build()
                    .parseSignedClaims(authorization.substring(7)).getPayload();
            id = claims.get("id", Long.class);
            if (id == null || id <= 0 || claims.getExpiration() == null) {
                throw new IllegalArgumentException("Sesión incompleta");
            }
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida o vencida");
        }
        if (!rol.equals(claims.get("role"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acción no permitida para este rol");
        }
        return id;
    }
}
