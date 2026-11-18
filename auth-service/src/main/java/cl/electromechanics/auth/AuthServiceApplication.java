package cl.electromechanics.auth.service;

import cl.electromechanics.auth.dto.LoginRequest;
import cl.electromechanics.auth.dto.RegisterRequest;
import cl.electromechanics.auth.entity.User;
import cl.electromechanics.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public User registerUser(RegisterRequest request) {
		if (userRepository.findByUsername(request.getUsername()).isPresent()) {
			throw new RuntimeException("El nombre de usuario ya existe");
		}

		User user = new User();
		user.setUsername(request.getUsername());
		// Encriptar la contraseña con BCrypt obligatoriamente
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(request.getRole().toUpperCase());

		return userRepository.save(user);
	}

	public User authenticate(LoginRequest request) {
		Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

		if (userOpt.isPresent()) {
			User user = userOpt.get();
			// Validar la contraseña ingresada contra el hash almacenado en BD
			if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
				return user;
			}
		}
		throw new RuntimeException("Credenciales inválidas");
	}
}