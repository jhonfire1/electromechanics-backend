package cl.electromechanics.auth;

import cl.electromechanics.auth.dto.LoginRequest;
import cl.electromechanics.auth.dto.RegisterRequest;
import cl.electromechanics.auth.entity.User;
import cl.electromechanics.auth.repository.UserRepository;
import cl.electromechanics.auth.security.JwtUtil;
import cl.electromechanics.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private final UserRepository usuarios = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JwtUtil jwt = mock(JwtUtil.class);
    private final AuthService auth = new AuthService(usuarios, encoder, jwt);

    @Test
    void autenticaConRolEIdentificadorDelUsuario() {
        LoginRequest datos = new LoginRequest();
        datos.setUsername("123456785");
        datos.setPassword("clave-demo");
        when(usuarios.findByUsername(datos.getUsername()))
                .thenReturn(Optional.of(new User(7L, datos.getUsername(), "hash", "TECNICO")));
        when(encoder.matches(datos.getPassword(), "hash")).thenReturn(true);
        when(jwt.generateToken(datos.getUsername(), "TECNICO", 7L)).thenReturn("token-de-prueba");

        var respuesta = auth.authenticate(datos);

        assertEquals(7L, respuesta.getId());
        assertEquals("TECNICO", respuesta.getRole());
        assertEquals("token-de-prueba", respuesta.getToken());
    }

    @Test
    void rechazaCredencialesInvalidas() {
        LoginRequest datos = new LoginRequest();
        datos.setUsername("123456785");
        datos.setPassword("incorrecta");
        when(usuarios.findByUsername(datos.getUsername()))
                .thenReturn(Optional.of(new User(7L, datos.getUsername(), "hash", "TECNICO")));

        var error = assertThrows(ResponseStatusException.class, () -> auth.authenticate(datos));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verifyNoInteractions(jwt);
    }

    @Test
    void rechazaRolesQueLaAplicacionNoReconoce() {
        RegisterRequest datos = new RegisterRequest();
        datos.setUsername("123456785");
        datos.setPassword("clave-demo");
        datos.setRole("INVITADO");

        var error = assertThrows(ResponseStatusException.class, () -> auth.registerUser(datos));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(usuarios, encoder, jwt);
    }
}
