package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.EmpresaDTO;
import br.com.unp.conectatech.dto.EmpresaRegistroRequest;
import br.com.unp.conectatech.dto.MessageResponse;
import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.service.RedatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/redator")
@Tag(name = "Redator", description = "Gerenciamento de vagas e empresas (requer role REDATOR)")
@SecurityRequirement(name = "bearerAuth")
public class RedatorController {

    private final RedatorService redatorService;

    public RedatorController(RedatorService redatorService) {
        this.redatorService = redatorService;
    }

    @GetMapping("/jobs")
    @Operation(summary = "Listar todas as vagas", description = "Retorna todas as vagas cadastradas no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de vagas"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer role REDATOR", content = @Content)
    })
    public ResponseEntity<List<VagaDTO>> listarVagas() {
        return ResponseEntity.ok(redatorService.listarVagas());
    }

    @PostMapping("/jobs")
    @Operation(summary = "Criar vaga manualmente", description = "Cadastra uma nova vaga com fonte ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga criada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<VagaDTO> criarVaga(@Valid @RequestBody VagaDTO dto) {
        return ResponseEntity.ok(redatorService.criarVaga(dto));
    }

    @PutMapping("/jobs/{id}")
    @Operation(summary = "Atualizar vaga", description = "Atualiza os dados de uma vaga existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga atualizada"),
            @ApiResponse(responseCode = "404", description = "Vaga nao encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<VagaDTO> atualizarVaga(@Parameter(description = "ID da vaga") @PathVariable Long id,
            @Valid @RequestBody VagaDTO dto) {
        return ResponseEntity.ok(redatorService.atualizarVaga(id, dto));
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Deletar vaga", description = "Remove permanentemente uma vaga do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga removida"),
            @ApiResponse(responseCode = "404", description = "Vaga nao encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<MessageResponse> deletarVaga(@Parameter(description = "ID da vaga") @PathVariable Long id) {
        redatorService.deletarVaga(id);
        return ResponseEntity.ok(new MessageResponse("Vaga removida com sucesso"));
    }

    @GetMapping("/empresas")
    @Operation(summary = "Listar todas as empresas", description = "Retorna todas as empresas cadastradas no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de empresas"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer role REDATOR", content = @Content)
    })
    public ResponseEntity<List<EmpresaDTO>> listarEmpresas() {
        return ResponseEntity.ok(redatorService.listarEmpresas());
    }

    @GetMapping("/empresas/{id}")
    @Operation(summary = "Buscar empresa por ID", description = "Retorna os dados de uma empresa especifica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa encontrada"),
            @ApiResponse(responseCode = "404", description = "Empresa nao encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<EmpresaDTO> buscarEmpresa(@Parameter(description = "ID da empresa") @PathVariable Long id) {
        return ResponseEntity.ok(redatorService.buscarEmpresa(id));
    }

    @PostMapping("/empresas")
    @Operation(summary = "Criar empresa", description = "Cadastra uma nova empresa no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa criada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou email/CNPJ ja cadastrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<EmpresaDTO> criarEmpresa(@Valid @RequestBody EmpresaRegistroRequest request) {
        return ResponseEntity.ok(redatorService.criarEmpresa(request));
    }

    @PutMapping("/empresas/{id}")
    @Operation(summary = "Atualizar empresa", description = "Atualiza os dados de uma empresa existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa atualizada"),
            @ApiResponse(responseCode = "404", description = "Empresa nao encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<EmpresaDTO> atualizarEmpresa(@Parameter(description = "ID da empresa") @PathVariable Long id,
            @Valid @RequestBody EmpresaDTO dto) {
        return ResponseEntity.ok(redatorService.atualizarEmpresa(id, dto));
    }

    @DeleteMapping("/empresas/{id}")
    @Operation(summary = "Deletar empresa", description = "Remove permanentemente uma empresa do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empresa removida"),
            @ApiResponse(responseCode = "404", description = "Empresa nao encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<MessageResponse> deletarEmpresa(@Parameter(description = "ID da empresa") @PathVariable Long id) {
        redatorService.deletarEmpresa(id);
        return ResponseEntity.ok(new MessageResponse("Empresa removida com sucesso"));
    }
}
