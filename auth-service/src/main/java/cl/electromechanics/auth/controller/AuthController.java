package cl.electromechanics.auth.controller;

import cl.electromechanics.auth.dto.AuthResponse;
import cl.electromechanics.auth.dto.LoginRequest;
import cl.electromechanics.auth.dto.RegisterRequest;
import cl.electromechanics.auth.dto.TecnicoResponse;
import cl.electromechanics.auth.security.JwtUtil;
import cl.electromechanics.auth.service.AuthService;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.authenticate(request);
    }

    @PostMapping("/register")
    public AuthResponse register(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @Valid @RequestBody RegisterRequest request) {
        exigirSupervisor(token);
        return authService.registerUser(request);
    }

    @GetMapping("/tecnicos")
    public List<TecnicoResponse> tecnicos(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token) {
        exigirSupervisor(token);
        return authService.listarTecnicos();
    }

    @GetMapping("/tecnicos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void comprobarTecnico(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable Long id) {
        exigirSupervisor(token);
        if (!authService.existeTecnico(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Técnico no encontrado");
        }
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Void> conflicto() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    private void exigirSupervisor(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Se requiere iniciar sesión");
        }
        String rol;
        try {
            rol = jwtUtil.extractAllClaims(token.substring(7)).get("role", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida o vencida");
        }
        if (!"SUPERVISOR".equals(rol)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere el rol supervisor");
        }
    }
}
