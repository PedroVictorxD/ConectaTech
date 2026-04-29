package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.EmpresaDTO;
import br.com.unp.conectatech.dto.ConfirmarEmailRequest;
import br.com.unp.conectatech.dto.EmpresaLoginRequest;
import br.com.unp.conectatech.dto.EmpresaLoginResponse;
import br.com.unp.conectatech.dto.EmpresaRegistroRequest;
import br.com.unp.conectatech.dto.AlterarSenhaRequest;
import br.com.unp.conectatech.dto.MessageResponse;
import br.com.unp.conectatech.dto.RecuperarSenhaRequest;
import br.com.unp.conectatech.service.EmpresaAuthService;
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
@RequestMapping(value = "/api/empresa/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Empresa Auth", description = "Registro e login de empresas")
public class EmpresaAuthController {

    private final EmpresaAuthService empresaAuthService;

    public EmpresaAuthController(EmpresaAuthService empresaAuthService) {
        this.empresaAuthService = empresaAuthService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar empresa", description = "Cria uma nova conta de empresa na plataforma")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa registrada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou email/CNPJ ja cadastrado", content = @Content)
    })
    public ResponseEntity<EmpresaDTO> register(@Valid @RequestBody EmpresaRegistroRequest request) {
        return ResponseEntity.ok(empresaAuthService.registrar(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login empresa", description = "Autentica a empresa e retorna token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado"),
            @ApiResponse(responseCode = "400", description = "Email, senha ou confirmacao invalidos", content = @Content)
    })
    public ResponseEntity<EmpresaLoginResponse> login(@Valid @RequestBody EmpresaLoginRequest request) {
        return ResponseEntity.ok(empresaAuthService.login(request));
    }

    @PostMapping("/confirm-email")
    @Operation(summary = "Confirmar email da empresa", description = "Confirma o email da empresa usando o token enviado no cadastro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email confirmado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token invalido ou expirado", content = @Content)
    })
    public ResponseEntity<MessageResponse> confirmarEmail(@Valid @RequestBody ConfirmarEmailRequest request) {
        empresaAuthService.confirmarEmail(request.getToken());
        return ResponseEntity.ok(new MessageResponse("Email confirmado com sucesso"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperacao de senha da empresa", description = "Gera um token de recuperacao e envia por email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email de recuperacao enviado"),
            @ApiResponse(responseCode = "404", description = "Email nao encontrado", content = @Content)
    })
    public ResponseEntity<MessageResponse> recuperarSenha(@Valid @RequestBody RecuperarSenhaRequest request) {
        empresaAuthService.recuperarSenha(request);
        return ResponseEntity.ok(new MessageResponse("Email de recuperacao enviado com sucesso"));
    }

    @PutMapping("/reset-password")
    @Operation(summary = "Alterar senha da empresa", description = "Altera a senha usando o token de recuperacao")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token invalido ou expirado", content = @Content)
    })
    public ResponseEntity<MessageResponse> alterarSenha(@Valid @RequestBody AlterarSenhaRequest request) {
        empresaAuthService.alterarSenha(request);
        return ResponseEntity.ok(new MessageResponse("Senha alterada com sucesso"));
    }
}
