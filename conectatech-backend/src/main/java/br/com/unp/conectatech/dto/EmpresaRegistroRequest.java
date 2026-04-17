package br.com.unp.conectatech.dto;

import br.com.unp.conectatech.validation.ValidCnpj;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Dados para registro de empresa")
public class EmpresaRegistroRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Schema(description = "Nome da empresa", example = "Tech Solutions LTDA")
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    @Schema(description = "Email da empresa", example = "contato@techsolutions.com")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
    @Schema(description = "Senha", example = "senha123")
    private String senha;

    @NotBlank(message = "CNPJ é obrigatório")
    @ValidCnpj
    @Schema(description = "CNPJ da empresa", example = "11.222.333/0001-81")
    private String cnpj;

    @NotBlank(message = "Telefone é obrigatório")
    @Schema(description = "Telefone", example = "(84) 99999-0000")
    private String telefone;

    @Schema(description = "Endereco", example = "Rua Principal, 100, Mossoro/RN")
    private String endereco;

    @Schema(description = "Descricao da empresa", example = "Empresa de tecnologia focada em solucoes web")
    private String descricao;

    @Schema(description = "Area de atuacao", example = "Tecnologia da Informacao")
    private String areaAtuacao;
}
