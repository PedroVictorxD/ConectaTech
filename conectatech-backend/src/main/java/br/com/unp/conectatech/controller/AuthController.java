package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.*;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.service.AuthService;
import br.com.unp.conectatech.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Auth", description = "Autenticacao e registro de usuarios")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar novo estudante", description = "Cria uma nova conta de estudante na plataforma")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou email ja cadastrado", content = @Content)
    })
    public ResponseEntity<UsuarioDTO> register(@Valid @RequestBody RegistroRequest request) {
        Usuario usuario = authService.registrar(
                request.getNome(), request.getEmail(), request.getSenha(),
                request.getCurso(), request.getPeriodo());
        return ResponseEntity.ok(usuarioService.toDTO(usuario));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica o usuario e retorna um token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Email, senha ou confirmacao invalidos", content = @Content)
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm-email")
    @Operation(summary = "Confirmar email", description = "Confirma o email da conta usando o token enviado no cadastro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email confirmado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token invalido ou expirado", content = @Content)
    })
    public ResponseEntity<MessageResponse> confirmarEmail(@Valid @RequestBody ConfirmarEmailRequest request) {
        authService.confirmarEmail(request.getToken());
        return ResponseEntity.ok(new MessageResponse("Email confirmado com sucesso"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperacao de senha", description = "Gera um token de recuperacao e envia por email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email de recuperacao enviado"),
            @ApiResponse(responseCode = "404", description = "Email nao encontrado", content = @Content)
    })
    public ResponseEntity<MessageResponse> recuperarSenha(@Valid @RequestBody RecuperarSenhaRequest request) {
        authService.recuperarSenha(request);
        return ResponseEntity.ok(new MessageResponse("Email de recuperacao enviado com sucesso"));
    }

    @PutMapping("/reset-password")
    @Operation(summary = "Alterar senha", description = "Altera a senha usando o token de recuperacao")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token invalido ou expirado", content = @Content)
    })
    public ResponseEntity<MessageResponse> alterarSenha(@Valid @RequestBody AlterarSenhaRequest request) {
        authService.alterarSenha(request);
        return ResponseEntity.ok(new MessageResponse("Senha alterada com sucesso"));
    }
}
