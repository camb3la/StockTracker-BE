package com.example.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "Username necessario")
    @Size(min = 3, max = 20, message = "Il nome utente deve essere compreso tra 3 e 20 caratteri")
    private String username;

    @NotBlank(message = "Email necessaria")
    @Size(max = 50, message = "L'email non può superare i 50 caratteri")
    @Email(message = "L'email deve essere valida")
    private String email;

    @NotBlank(message = "La password è obbligatoria")
    @Size(min = 6, max = 40, message = "La password deve essere compresa tra 6 e 40 caratteri")
    private String password;

}
