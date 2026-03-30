package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Dados para atualizar perfil do estudante")
public class AtualizarUsuarioRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Schema(description = "Nome completo", example = "Joao Silva")
    private String nome;

    @Schema(description = "Curso do estudante", example = "Engenharia de Software")
    private String curso;

    @Schema(description = "Periodo atual", example = "7")
    private String periodo;
}
