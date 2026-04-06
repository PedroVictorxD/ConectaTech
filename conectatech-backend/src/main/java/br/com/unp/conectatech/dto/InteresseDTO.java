package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados de um interesse em uma vaga")
public class InteresseDTO {

    @Schema(description = "ID da vaga", example = "1")
    private Long vagaId;

    @Schema(description = "Nome do aluno", example = "Pedro Victor")
    private String alunoNome;

    @Schema(description = "Email do aluno", example = "pedro@email.com")
    private String alunoEmail;

    @Schema(description = "Curso do aluno", example = "Ciencia da Computacao")
    private String alunoCurso;

    @Schema(description = "Data do interesse", example = "06/04/2026 14:30")
    private String dataInteresse;
}
