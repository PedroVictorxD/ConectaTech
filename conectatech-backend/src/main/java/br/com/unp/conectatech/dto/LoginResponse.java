package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Resposta de login com token JWT")
public class LoginResponse {

    @Schema(description = "Token JWT para autenticacao", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Nome do usuario", example = "Joao Silva")
    private String nome;

    @Schema(description = "Email do usuario", example = "aluno@email.com")
    private String email;

    @Schema(description = "Role do usuario", example = "STUDENT")
    private String role;
}
