package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Dados para solicitar recuperacao de senha")
public class RecuperarSenhaRequest {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Schema(description = "Email cadastrado", example = "aluno@email.com")
    private String email;
}
