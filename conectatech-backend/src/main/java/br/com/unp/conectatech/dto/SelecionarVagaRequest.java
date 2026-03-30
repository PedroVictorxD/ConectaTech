package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Dados para selecionar uma vaga")
public class SelecionarVagaRequest {

    @NotNull(message = "ID da vaga é obrigatório")
    @Schema(description = "ID da vaga a ser selecionada", example = "1")
    private Long vagaId;
}
