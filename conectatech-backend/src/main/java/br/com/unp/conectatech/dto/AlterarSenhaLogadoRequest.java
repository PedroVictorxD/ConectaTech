package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Dados para alterar senha estando autenticado")
public class AlterarSenhaLogadoRequest {

    @NotBlank(message = "Senha atual é obrigatória")
    @Schema(description = "Senha atual do usuario", example = "minhasenha123")
    private String senhaAtual;

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    @Schema(description = "Nova senha (min 6 caracteres)", example = "novasenha456")
    private String novaSenha;
}
