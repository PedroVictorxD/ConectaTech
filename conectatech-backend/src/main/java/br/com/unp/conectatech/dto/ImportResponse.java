package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Resposta de importacao de vagas")
public class ImportResponse {

    @Schema(description = "Mensagem de retorno", example = "Importacao concluida")
    private String message;

    @Schema(description = "Quantidade de vagas importadas", example = "15")
    private int vagasImportadas;
}
