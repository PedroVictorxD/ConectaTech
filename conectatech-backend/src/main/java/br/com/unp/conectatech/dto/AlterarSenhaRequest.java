package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Dados para alterar senha com token de recuperacao")
public class AlterarSenhaRequest {

    @NotBlank(message = "Token é obrigatório")
    @Schema(description = "Token recebido na recuperacao de senha", example = "550e8400-e29b-41d4-a716-446655440000")
    private String token;

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    @Schema(description = "Nova senha (min 6 caracteres)", example = "novasenha456")
    private String novaSenha;
}
