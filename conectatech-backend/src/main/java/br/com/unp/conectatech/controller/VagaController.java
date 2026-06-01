package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.MessageResponse;
import br.com.unp.conectatech.dto.SelecionarVagaRequest;
import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.service.InteresseService;
import br.com.unp.conectatech.service.VagaSelecionadaService;
import br.com.unp.conectatech.service.VagaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Vagas", description = "Busca e selecao de vagas de estagio")
public class VagaController {

    private final VagaService vagaService;
    private final VagaSelecionadaService vagaSelecionadaService;
    private final InteresseService interesseService;

    public VagaController(VagaService vagaService, VagaSelecionadaService vagaSelecionadaService,
            InteresseService interesseService) {
        this.vagaService = vagaService;
        this.vagaSelecionadaService = vagaSelecionadaService;
        this.interesseService = interesseService;
    }

    @GetMapping
    @Operation(summary = "Listar vagas", description = "Lista vagas com filtros opcionais: texto, localizacao, fonte e datas de publicacao")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de vagas retornada"),
            @ApiResponse(responseCode = "400", description = "Formato de data invalido — use yyyy-MM-dd", content = @Content)
    })
    public ResponseEntity<?> listarTodas(
            @Parameter(description = "Texto para buscar em titulo, empresa e descricao") @RequestParam(required = false) String busca,
            @Parameter(description = "Filtrar por localizacao (ex: Mossoro)") @RequestParam(required = false) String localizacao,
            @Parameter(description = "Filtrar por fonte: ADMIN ou EMPRESA") @RequestParam(required = false) String fonte,
            @Parameter(description = "Data inicio no formato yyyy-MM-dd") @RequestParam(required = false) String dataInicio,
            @Parameter(description = "Data fim no formato yyyy-MM-dd") @RequestParam(required = false) String dataFim) {
        try {
            return ResponseEntity.ok(vagaService.buscarComFiltros(busca, localizacao, fonte, dataInicio, dataFim));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Formato de data invalido. Use yyyy-MM-dd (ex: 2026-01-31)"));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar vaga por ID", description = "Retorna os detalhes de uma vaga especifica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga encontrada"),
            @ApiResponse(responseCode = "404", description = "Vaga nao encontrada", content = @Content)
    })
    public ResponseEntity<VagaDTO> buscarPorId(@Parameter(description = "ID da vaga") @PathVariable Long id) {
        return ResponseEntity.ok(vagaService.buscarPorId(id));
    }

    @PostMapping("/select")
    @Operation(summary = "Selecionar vaga", description = "Adiciona uma vaga a lista de vagas selecionadas do estudante", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga selecionada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Vaga nao encontrada", content = @Content)
    })
    public ResponseEntity<MessageResponse> selecionar(@Valid @RequestBody SelecionarVagaRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        Long vagaId = request.getVagaId();
        vagaSelecionadaService.selecionar(email, vagaId);
        return ResponseEntity.ok(new MessageResponse("Vaga selecionada com sucesso"));
    }

    @GetMapping("/my-selections")
    @Operation(summary = "Minhas vagas", description = "Lista as vagas selecionadas pelo estudante autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de vagas selecionadas"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido", content = @Content)
    })
    public ResponseEntity<List<VagaDTO>> minhasVagas(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(vagaSelecionadaService.listarMinhasVagas(email));
    }

    @DeleteMapping("/my-selections/{vagaId}")
    @Operation(summary = "Remover vaga selecionada", description = "Remove uma vaga da lista de selecionadas do estudante", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga removida das selecionadas"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido", content = @Content)
    })
    public ResponseEntity<MessageResponse> removerSelecionada(
            @Parameter(description = "ID da vaga") @PathVariable Long vagaId,
            Authentication authentication) {
        String email = authentication.getName();
        vagaSelecionadaService.removerSelecionada(email, vagaId);
        return ResponseEntity.ok(new MessageResponse("Vaga removida das selecionadas"));
    }

    @PostMapping("/{id}/interesse")
    @Operation(summary = "Demonstrar interesse", description = "Aluno demonstra interesse em uma vaga", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interesse registrado"),
            @ApiResponse(responseCode = "400", description = "Interesse ja demonstrado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Vaga nao encontrada", content = @Content)
    })
    public ResponseEntity<MessageResponse> demonstrarInteresse(@PathVariable Long id, Authentication authentication) {
        interesseService.demonstrarInteresse(authentication.getName(), id);
        return ResponseEntity.ok(new MessageResponse("Interesse demonstrado com sucesso"));
    }

    @DeleteMapping("/{id}/interesse")
    @Operation(summary = "Remover interesse", description = "Aluno remove interesse em uma vaga", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Interesse removido"),
            @ApiResponse(responseCode = "404", description = "Interesse nao encontrado", content = @Content)
    })
    public ResponseEntity<MessageResponse> removerInteresse(@PathVariable Long id, Authentication authentication) {
        interesseService.removerInteresse(authentication.getName(), id);
        return ResponseEntity.ok(new MessageResponse("Interesse removido com sucesso"));
    }
}
