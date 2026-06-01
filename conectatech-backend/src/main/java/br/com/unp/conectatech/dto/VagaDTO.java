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
@Schema(description = "Dados de uma vaga de estagio")
public class VagaDTO {

    @Schema(description = "ID da vaga", example = "1")
    private Long id;

    @NotBlank(message = "Titulo é obrigatório")
    @Schema(description = "Titulo da vaga", example = "Estagiario de Desenvolvimento Web")
    private String titulo;

    @NotBlank(message = "Empresa é obrigatória")
    @Schema(description = "Nome da empresa", example = "Tech Solutions LTDA")
    private String empresa;

    @Schema(description = "Descricao da vaga", example = "Vaga para desenvolvimento web com React e Spring Boot")
    private String descricao;

    @Schema(description = "Localizacao da vaga", example = "Mossoro, RN")
    private String localizacao;

    @Schema(description = "URL da vaga original", example = "https://exemplo.com/vaga/123")
    private String url;

    @Schema(description = "Fonte da vaga", example = "ADMIN")
    private String fonte;

    @Schema(description = "Nome da empresa vinculada", example = "Tech Solutions LTDA")
    private String empresaNome;

    @Schema(description = "Data de publicacao", example = "30/03/2026 10:00")
    private String dataPublicacao;
}
