package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.*;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.service.AuthService;
import br.com.unp.conectatech.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> register(@Valid @RequestBody RegistroRequest request) {
        Usuario usuario = authService.registrar(
                request.getNome(), request.getEmail(), request.getSenha(),
                request.getCurso(), request.getPeriodo());
        return ResponseEntity.ok(usuarioService.toDTO(usuario));
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
