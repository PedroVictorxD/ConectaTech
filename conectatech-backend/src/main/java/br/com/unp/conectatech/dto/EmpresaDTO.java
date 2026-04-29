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
@Schema(description = "Dados da empresa")
public class EmpresaDTO {

    @Schema(description = "ID da empresa", example = "1")
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @Schema(description = "Nome da empresa", example = "Tech Solutions LTDA")
    private String nome;

    @Schema(description = "Email", example = "contato@techsolutions.com")
    private String email;

    @Schema(description = "CNPJ", example = "12.345.678/0001-99")
    private String cnpj;

    @Schema(description = "Telefone", example = "(84) 99999-0000")
    private String telefone;

    @Schema(description = "Endereco", example = "Rua Principal, 100")
    private String endereco;

    @Schema(description = "Descricao", example = "Empresa de tecnologia")
    private String descricao;

    @Schema(description = "Area de atuacao", example = "TI")
    private String areaAtuacao;

    @Schema(description = "Indica se o email da empresa ja foi confirmado", example = "false")
    private Boolean emailVerificado;

    @Schema(description = "Data de criacao", example = "06/04/2026 14:30")
    private String criadoEm;
}
