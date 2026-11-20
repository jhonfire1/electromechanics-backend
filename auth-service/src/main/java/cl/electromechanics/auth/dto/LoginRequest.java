package cl.electromechanics.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class LoginRequest {
    @NotBlank @Size(max = 80)
    private String username;
    @NotBlank @Size(max = 72)
    private String password;
}
