package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Resposta com mensagem de status")
public class MessageResponse {

    @Schema(description = "Mensagem de retorno", example = "Operacao realizada com sucesso")
    private String message;
}
