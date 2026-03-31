package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados do usuario")
public class UsuarioDTO {

    @Schema(description = "ID do usuario", example = "1")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Schema(description = "Nome completo", example = "Pedro Victor")
    private String nome;

    @Schema(description = "Email", example = "pedro@email.com")
    private String email;

    @Schema(description = "Curso", example = "Ciencia da Computacao")
    private String curso;

    @Schema(description = "Periodo", example = "6")
    private String periodo;

    @Schema(description = "Role do usuario", example = "STUDENT")
    private String role;

    @Schema(description = "Data de criacao", example = "30/03/2026 14:30")
    private String criadoEm;
}
