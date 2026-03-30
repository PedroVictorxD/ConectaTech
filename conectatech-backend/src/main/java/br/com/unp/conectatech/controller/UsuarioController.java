package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.AtualizarUsuarioRequest;
import br.com.unp.conectatech.dto.UsuarioDTO;
import br.com.unp.conectatech.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meus-dados")
@Tag(name = "Usuario", description = "Perfil do estudante autenticado")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(summary = "Ver meus dados", description = "Retorna os dados do estudante autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do usuario"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido")
    })
    public ResponseEntity<UsuarioDTO> meusDados(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(usuarioService.buscarPorEmail(email));
    }

    @PutMapping
    @Operation(summary = "Atualizar meus dados", description = "Atualiza nome, curso e periodo do estudante autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados atualizados"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido")
    })
    public ResponseEntity<UsuarioDTO> atualizar(@Valid @RequestBody AtualizarUsuarioRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(usuarioService.atualizar(email, request));
    }
}
