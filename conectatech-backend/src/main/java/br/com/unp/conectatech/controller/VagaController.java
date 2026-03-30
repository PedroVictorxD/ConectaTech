package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.SelecionarVagaRequest;
import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.service.JoobleService;
import br.com.unp.conectatech.service.VagaSelecionadaService;
import br.com.unp.conectatech.service.VagaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import br.com.unp.conectatech.dto.MessageResponse;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Vagas", description = "Busca e selecao de vagas de estagio")
public class VagaController {

    private final VagaService vagaService;
    private final VagaSelecionadaService vagaSelecionadaService;
    private final JoobleService joobleService;

    public VagaController(VagaService vagaService, VagaSelecionadaService vagaSelecionadaService, JoobleService joobleService) {
        this.vagaService = vagaService;
        this.vagaSelecionadaService = vagaSelecionadaService;
        this.joobleService = joobleService;
    }

    @GetMapping
    @Operation(summary = "Listar vagas", description = "Lista todas as vagas com filtros opcionais por texto, localizacao e fonte")
    @ApiResponse(responseCode = "200", description = "Lista de vagas retornada")
    public ResponseEntity<List<VagaDTO>> listarTodas(
            @Parameter(description = "Texto para buscar em titulo, empresa e descricao") @RequestParam(required = false) String busca,
            @Parameter(description = "Filtrar por localizacao (ex: Mossoro)") @RequestParam(required = false) String localizacao,
            @Parameter(description = "Filtrar por fonte: JOOBLE, JSOUP ou ADMIN") @RequestParam(required = false) String fonte) {
        List<VagaDTO> vagas;
        if ((busca != null && !busca.isBlank()) || (localizacao != null && !localizacao.isBlank()) || (fonte != null && !fonte.isBlank())) {
            vagas = vagaService.buscarComFiltros(busca, localizacao, fonte);
        } else {
            vagas = vagaService.listarTodas();
        }
        return ResponseEntity.ok(vagas);
    }

    @GetMapping("/search-external")
    @Operation(summary = "Buscar vagas externas", description = "Importa vagas do Jooble e retorna resultados do banco")
    @ApiResponse(responseCode = "200", description = "Vagas externas importadas e retornadas")
    public ResponseEntity<List<VagaDTO>> buscarExternas(
            @Parameter(description = "Palavras-chave para busca") @RequestParam(defaultValue = "estagio") String keywords,
            @Parameter(description = "Localizacao da busca") @RequestParam(defaultValue = "Mossoro, RN") String location) {
        joobleService.buscarVagasJooble(keywords, location);
        List<VagaDTO> vagas;
        if (!keywords.isBlank()) {
            vagas = vagaService.buscarPorTitulo(keywords);
        } else {
            vagas = vagaService.listarTodas();
        }
        return ResponseEntity.ok(vagas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar vaga por ID", description = "Retorna os detalhes de uma vaga especifica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga encontrada"),
            @ApiResponse(responseCode = "404", description = "Vaga nao encontrada")
    })
    public ResponseEntity<VagaDTO> buscarPorId(@Parameter(description = "ID da vaga") @PathVariable Long id) {
        return ResponseEntity.ok(vagaService.buscarPorId(id));
    }

    @PostMapping("/select")
    @Operation(summary = "Selecionar vaga", description = "Adiciona uma vaga a lista de vagas selecionadas do estudante", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga selecionada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido"),
            @ApiResponse(responseCode = "404", description = "Vaga nao encontrada")
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
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido")
    })
    public ResponseEntity<List<VagaDTO>> minhasVagas(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(vagaSelecionadaService.listarMinhasVagas(email));
    }

    @DeleteMapping("/my-selections/{vagaId}")
    @Operation(summary = "Remover vaga selecionada", description = "Remove uma vaga da lista de selecionadas do estudante", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga removida das selecionadas"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido")
    })
    public ResponseEntity<MessageResponse> removerSelecionada(@Parameter(description = "ID da vaga") @PathVariable Long vagaId,
            Authentication authentication) {
        String email = authentication.getName();
        vagaSelecionadaService.removerSelecionada(email, vagaId);
        return ResponseEntity.ok(new MessageResponse("Vaga removida das selecionadas"));
    }
}
