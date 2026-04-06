package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.EmpresaDTO;
import br.com.unp.conectatech.dto.MessageResponse;
import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.service.EmpresaService;
import br.com.unp.conectatech.service.EmpresaVagaService;
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

import java.util.List;

@RestController
@RequestMapping("/api/empresa")
@Tag(name = "Empresa", description = "Gestao de vagas e perfil da empresa autenticada")
@SecurityRequirement(name = "bearerAuth")
public class EmpresaController {

    private final EmpresaVagaService empresaVagaService;
    private final EmpresaService empresaService;

    public EmpresaController(EmpresaVagaService empresaVagaService, EmpresaService empresaService) {
        this.empresaVagaService = empresaVagaService;
        this.empresaService = empresaService;
    }

    @GetMapping("/jobs")
    @Operation(summary = "Listar vagas da empresa")
    @ApiResponse(responseCode = "200", description = "Lista de vagas da empresa")
    public ResponseEntity<List<VagaDTO>> listarVagas(Authentication authentication) {
        return ResponseEntity.ok(empresaVagaService.listarVagas(authentication.getName()));
    }

    @PostMapping("/jobs")
    @Operation(summary = "Criar vaga")
    @ApiResponse(responseCode = "200", description = "Vaga criada")
    public ResponseEntity<VagaDTO> criarVaga(@Valid @RequestBody VagaDTO dto, Authentication authentication) {
        return ResponseEntity.ok(empresaVagaService.criarVaga(authentication.getName(), dto));
    }

    @PutMapping("/jobs/{id}")
    @Operation(summary = "Atualizar vaga")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga atualizada"),
            @ApiResponse(responseCode = "403", description = "Vaga nao pertence a esta empresa", content = @Content),
            @ApiResponse(responseCode = "404", description = "Vaga nao encontrada", content = @Content)
    })
    public ResponseEntity<VagaDTO> atualizarVaga(@PathVariable Long id, @Valid @RequestBody VagaDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(empresaVagaService.atualizarVaga(authentication.getName(), id, dto));
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Deletar vaga")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga removida"),
            @ApiResponse(responseCode = "403", description = "Vaga nao pertence a esta empresa", content = @Content)
    })
    public ResponseEntity<MessageResponse> deletarVaga(@PathVariable Long id, Authentication authentication) {
        empresaVagaService.deletarVaga(authentication.getName(), id);
        return ResponseEntity.ok(new MessageResponse("Vaga removida com sucesso"));
    }

    @GetMapping("/perfil")
    @Operation(summary = "Ver perfil da empresa")
    @ApiResponse(responseCode = "200", description = "Dados da empresa")
    public ResponseEntity<EmpresaDTO> perfil(Authentication authentication) {
        return ResponseEntity.ok(empresaService.buscarPorEmail(authentication.getName()));
    }

    @PutMapping("/perfil")
    @Operation(summary = "Atualizar perfil da empresa")
    @ApiResponse(responseCode = "200", description = "Perfil atualizado")
    public ResponseEntity<EmpresaDTO> atualizarPerfil(@Valid @RequestBody EmpresaDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(empresaService.atualizarPorEmail(authentication.getName(), dto));
    }
}
