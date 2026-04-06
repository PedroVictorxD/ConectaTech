package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.EmpresaDTO;
import br.com.unp.conectatech.dto.EmpresaLoginRequest;
import br.com.unp.conectatech.dto.EmpresaLoginResponse;
import br.com.unp.conectatech.dto.EmpresaRegistroRequest;
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
            @ApiResponse(responseCode = "400", description = "Email ou senha invalidos", content = @Content)
    })
    public ResponseEntity<EmpresaLoginResponse> login(@Valid @RequestBody EmpresaLoginRequest request) {
        return ResponseEntity.ok(empresaAuthService.login(request));
    }
}
