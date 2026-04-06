package br.com.unp.conectatech.controller;

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
@Tag(name = "Redator", description = "Gerenciamento de vagas (requer role REDATOR)")
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
}
