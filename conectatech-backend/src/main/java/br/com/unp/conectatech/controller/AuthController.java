package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.AlterarSenhaRequest;
import br.com.unp.conectatech.dto.LoginRequest;
import br.com.unp.conectatech.dto.LoginResponse;
import br.com.unp.conectatech.dto.RecuperarSenhaRequest;
import br.com.unp.conectatech.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recuperar-senha")
    public ResponseEntity<Map<String, String>> recuperarSenha(@Valid @RequestBody RecuperarSenhaRequest request) {
        authService.recuperarSenha(request);
        return ResponseEntity.ok(Map.of("message", "Token de recuperação gerado com sucesso"));
    }

    @PutMapping("/alterar-senha")
    public ResponseEntity<Map<String, String>> alterarSenha(@Valid @RequestBody AlterarSenhaRequest request) {
        authService.alterarSenha(request);
        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso"));
    }
}
