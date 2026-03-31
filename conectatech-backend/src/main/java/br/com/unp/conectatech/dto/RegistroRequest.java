package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Dados para registro de novo estudante")
public class RegistroRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Schema(description = "Nome completo", example = "Pedro Victor")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Schema(description = "Email do estudante", example = "pedro@email.com")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    @Schema(description = "Senha (min 6 caracteres)", example = "minhasenha123")
    private String senha;

    @Schema(description = "Curso do estudante", example = "Ciencia da Computacao")
    private String curso;

    @Schema(description = "Periodo atual", example = "6")
    private String periodo;
}
