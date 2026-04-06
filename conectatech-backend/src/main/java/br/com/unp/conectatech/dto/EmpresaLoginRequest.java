package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Dados para login de empresa")
public class EmpresaLoginRequest {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Schema(description = "Email da empresa", example = "contato@techsolutions.com")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Schema(description = "Senha", example = "senha123")
    private String senha;
}
