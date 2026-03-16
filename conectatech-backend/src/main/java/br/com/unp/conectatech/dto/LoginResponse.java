package br.com.unp.conectatech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String nome;
    private String email;
    private String role;
}
