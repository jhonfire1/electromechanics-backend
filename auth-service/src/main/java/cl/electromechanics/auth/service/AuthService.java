package cl.electromechanics.auth.service;

import cl.electromechanics.auth.dto.AuthResponse;
import cl.electromechanics.auth.dto.LoginRequest;
import cl.electromechanics.auth.dto.RegisterRequest;
import cl.electromechanics.auth.dto.TecnicoResponse;
import cl.electromechanics.auth.entity.User;
import cl.electromechanics.auth.repository.UserRepository;
import cl.electromechanics.auth.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse registerUser(RegisterRequest request) {
        String username = request.getUsername().strip();
        String role = request.getRole().strip().toUpperCase(Locale.ROOT);
        if (!"TECNICO".equals(role) && !"SUPERVISOR".equals(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no permitido");
        }
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contraseña demasiado larga");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User saved = userRepository.save(user);
        return new AuthResponse(null, saved.getId(), saved.getUsername(), saved.getRole());
    }

    public List<TecnicoResponse> listarTecnicos() {
        return userRepository.findByRoleOrderByUsernameAsc("TECNICO").stream()
                .map(user -> new TecnicoResponse(user.getId(), user.getUsername()))
                .toList();
    }

    public boolean existeTecnico(Long id) {
        return userRepository.existsByIdAndRole(id, "TECNICO");
    }

    public AuthResponse authenticate(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId());
                return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    }
}
