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
        User saved = guardarUsuario(username, request.getPassword(), role);
        return new AuthResponse(null, saved.getId(), saved.getUsername(), saved.getRole());
    }

    public AuthResponse registrarCliente(LoginRequest request) {
        String rut = request.getUsername().strip().replace(".", "").replace("-", "")
                .toUpperCase(Locale.ROOT);
        if (!rutValido(rut) || request.getPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUT o contraseña inválidos");
        }
        User cliente = guardarUsuario(rut, request.getPassword(), "CLIENTE");
        String token = jwtUtil.generateToken(cliente.getUsername(), cliente.getRole(), cliente.getId());
        return new AuthResponse(token, cliente.getId(), cliente.getUsername(), cliente.getRole());
    }

    private User guardarUsuario(String username, String password, String role) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contraseña demasiado larga");
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de usuario ya existe");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return userRepository.save(user);
    }

    private boolean rutValido(String rut) {
        if (!rut.matches("[0-9]{7,8}[0-9K]")) return false;
        int suma = 0;
        int orden = 0;
        for (int i = rut.length() - 2; i >= 0; i--) {
            suma += (rut.charAt(i) - '0') * (2 + orden++ % 6);
        }
        int indice = (11 - suma % 11) % 11;
        return rut.charAt(rut.length() - 1) == "0123456789K".charAt(indice);
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
