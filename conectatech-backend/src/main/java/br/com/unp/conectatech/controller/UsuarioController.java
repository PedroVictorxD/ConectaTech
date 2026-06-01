package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.AlterarSenhaLogadoRequest;
import br.com.unp.conectatech.dto.AtualizarUsuarioRequest;
import br.com.unp.conectatech.dto.MessageResponse;
import br.com.unp.conectatech.dto.UsuarioDTO;
import br.com.unp.conectatech.service.AuthService;
import br.com.unp.conectatech.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Usuario", description = "Perfil do estudante autenticado")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    public UsuarioController(UsuarioService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
    }

    @GetMapping
    @Operation(summary = "Ver meus dados", description = "Retorna os dados do estudante autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do usuario"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido", content = @Content)
    })
    public ResponseEntity<UsuarioDTO> meusDados(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(usuarioService.buscarPorEmail(email));
    }

    @PutMapping
    @Operation(summary = "Atualizar meus dados", description = "Atualiza nome, curso e periodo do estudante autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados atualizados"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido", content = @Content)
    })
    public ResponseEntity<UsuarioDTO> atualizar(@Valid @RequestBody AtualizarUsuarioRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(usuarioService.atualizar(email, request));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Alterar senha", description = "Altera a senha do estudante autenticado informando a senha atual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Senha atual incorreta ou nova senha invalida", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido", content = @Content)
    })
    public ResponseEntity<MessageResponse> alterarSenha(@Valid @RequestBody AlterarSenhaLogadoRequest request,
            Authentication authentication) {
        authService.alterarSenhaLogado(authentication.getName(), request);
        return ResponseEntity.ok(new MessageResponse("Senha alterada com sucesso"));
    }

    @DeleteMapping
    @Operation(summary = "Excluir minha conta", description = "Remove permanentemente a conta do estudante autenticado (LGPD)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta excluida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido", content = @Content)
    })
    public ResponseEntity<MessageResponse> deletarConta(Authentication authentication) {
        usuarioService.deletarConta(authentication.getName());
        return ResponseEntity.ok(new MessageResponse("Conta excluida com sucesso"));
    }
}
