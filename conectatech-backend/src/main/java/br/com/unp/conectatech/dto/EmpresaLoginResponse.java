package br.com.unp.conectatech.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Resposta de login de empresa")
public class EmpresaLoginResponse {

    @Schema(description = "Token JWT", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Nome da empresa", example = "Tech Solutions LTDA")
    private String nome;

    @Schema(description = "Email da empresa", example = "contato@techsolutions.com")
    private String email;

    @Schema(description = "CNPJ", example = "12.345.678/0001-99")
    private String cnpj;
}
