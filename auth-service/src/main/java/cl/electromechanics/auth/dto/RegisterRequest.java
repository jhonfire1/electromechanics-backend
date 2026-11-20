package cl.electromechanics.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class RegisterRequest {
    @NotBlank @Size(max = 80)
    private String username;
    @NotBlank @Size(min = 4, max = 72)
    private String password;
    @NotBlank @Size(max = 20)
    private String role; // "TECNICO" o "SUPERVISOR"
}
