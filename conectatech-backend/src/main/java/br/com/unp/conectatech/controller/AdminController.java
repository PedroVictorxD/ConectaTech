package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.ImportResponse;
import br.com.unp.conectatech.dto.InteresseDTO;
import br.com.unp.conectatech.dto.MessageResponse;
import br.com.unp.conectatech.dto.UsuarioDTO;
import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.service.AdminService;
import br.com.unp.conectatech.service.InteresseService;
import br.com.unp.conectatech.service.JoobleService;
import br.com.unp.conectatech.service.PrefeituraScraperService;
import br.com.unp.conectatech.service.ScraperService;
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
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Gerenciamento de usuarios e vagas (requer role ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final ScraperService scraperService;
    private final JoobleService joobleService;
    private final PrefeituraScraperService prefeituraScraperService;
    private final InteresseService interesseService;

    public AdminController(AdminService adminService, ScraperService scraperService,
            JoobleService joobleService, PrefeituraScraperService prefeituraScraperService,
            InteresseService interesseService) {
        this.adminService = adminService;
        this.scraperService = scraperService;
        this.joobleService = joobleService;
        this.prefeituraScraperService = prefeituraScraperService;
        this.interesseService = interesseService;
    }

    @GetMapping("/users")
    @Operation(summary = "Listar usuarios", description = "Retorna todos os usuarios cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - requer role ADMIN", content = @Content)
    })
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(adminService.listarUsuarios());
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Buscar usuario por ID", description = "Retorna os dados de um usuario especifico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<UsuarioDTO> buscarUsuario(@Parameter(description = "ID do usuario") @PathVariable Long id) {
        return ResponseEntity.ok(adminService.buscarUsuarioPorId(id));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Atualizar usuario", description = "Atualiza os dados de um usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario atualizado"),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<UsuarioDTO> atualizarUsuario(@Parameter(description = "ID do usuario") @PathVariable Long id,
            @Valid @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(adminService.atualizarUsuario(id, dto));
    }

    @PostMapping("/import-scraper")
    @Operation(summary = "Executar scraping", description = "Executa o scraper manualmente para importar vagas de sites externos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Scraping executado com quantidade de vagas importadas"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<ImportResponse> importarScraper() {
        List<Vaga> vagas = scraperService.executarScraping();
        return ResponseEntity.ok(new ImportResponse("Scraping executado", vagas.size()));
    }

    @PostMapping("/import-jooble")
    @Operation(summary = "Importar vagas do Jooble", description = "Busca e importa vagas da API Jooble para o banco de dados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Importacao concluida com quantidade de vagas"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<ImportResponse> importarJooble(
            @Parameter(description = "Palavras-chave para busca") @RequestParam(defaultValue = "estagio tecnologia") String keywords,
            @Parameter(description = "Localizacao da busca") @RequestParam(defaultValue = "Mossoro, RN") String location) {
        List<Vaga> vagas = joobleService.buscarVagasJooble(keywords, location);
        return ResponseEntity.ok(new ImportResponse("Importacao Jooble concluida", vagas.size()));
    }

    @GetMapping("/jobs")
    @Operation(summary = "Listar todas as vagas (admin)", description = "Retorna todas as vagas cadastradas no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de vagas"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<List<VagaDTO>> listarVagas() {
        return ResponseEntity.ok(adminService.listarVagas());
    }

    @PostMapping("/jobs")
    @Operation(summary = "Criar vaga manualmente", description = "Cadastra uma nova vaga com fonte ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga criada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<VagaDTO> criarVaga(@Valid @RequestBody VagaDTO dto) {
        return ResponseEntity.ok(adminService.criarVaga(dto));
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
        return ResponseEntity.ok(adminService.atualizarVaga(id, dto));
    }

    @PostMapping("/import-prefeitura")
    @Operation(summary = "Importar vagas da Prefeitura", description = "Busca e importa vagas do Painel de Empregos da Prefeitura de Mossoro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Importacao concluida com quantidade de vagas"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<ImportResponse> importarPrefeitura() {
        List<Vaga> vagas = prefeituraScraperService.importarVagas();
        return ResponseEntity.ok(new ImportResponse("Importacao Prefeitura concluida", vagas.size()));
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Deletar vaga", description = "Remove permanentemente uma vaga do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vaga removida"),
            @ApiResponse(responseCode = "404", description = "Vaga nao encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<MessageResponse> deletarVaga(@Parameter(description = "ID da vaga") @PathVariable Long id) {
        adminService.deletarVaga(id);
        return ResponseEntity.ok(new MessageResponse("Vaga removida com sucesso"));
    }

    @GetMapping("/jobs/{id}/interessados")
    @Operation(summary = "Ver interessados em uma vaga", description = "Lista os alunos que demonstraram interesse")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de interessados"),
            @ApiResponse(responseCode = "403", description = "Acesso negado", content = @Content)
    })
    public ResponseEntity<List<InteresseDTO>> listarInteressados(@PathVariable Long id) {
        return ResponseEntity.ok(interesseService.listarInteressadosPorVaga(id));
    }
}
