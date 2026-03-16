package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.AtualizarUsuarioRequest;
import br.com.unp.conectatech.dto.UsuarioDTO;
import br.com.unp.conectatech.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meus-dados")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<UsuarioDTO> meusDados(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(usuarioService.buscarPorEmail(email));
    }

    @PutMapping
    public ResponseEntity<UsuarioDTO> atualizar(@Valid @RequestBody AtualizarUsuarioRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(usuarioService.atualizar(email, request));
    }
}
