package br.com.unp.conectatech.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AtualizarUsuarioRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private String curso;

    private String periodo;
}
