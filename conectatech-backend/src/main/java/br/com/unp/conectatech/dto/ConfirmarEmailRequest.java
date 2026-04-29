package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Dados para confirmar email da conta")
public class ConfirmarEmailRequest {

    @NotBlank(message = "Token é obrigatório")
    @Schema(description = "Token recebido no email de confirmacao", example = "550e8400-e29b-41d4-a716-446655440000")
    private String token;
}
