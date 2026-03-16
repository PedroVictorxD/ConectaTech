package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.UsuarioDTO;
import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.service.AdminService;
import br.com.unp.conectatech.service.JoobleService;
import br.com.unp.conectatech.service.ScraperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final ScraperService scraperService;
    private final JoobleService joobleService;

    public AdminController(AdminService adminService, ScraperService scraperService, JoobleService joobleService) {
        this.adminService = adminService;
        this.scraperService = scraperService;
        this.joobleService = joobleService;
    }

    // === Usuários ===

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        return ResponseEntity.ok(adminService.listarUsuarios());
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDTO> buscarUsuario(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.buscarUsuarioPorId(id));
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<UsuarioDTO> atualizarUsuario(@PathVariable Long id,
            @RequestBody UsuarioDTO dto) {
        return ResponseEntity.ok(adminService.atualizarUsuario(id, dto));
    }

    // === Vagas ===

    @GetMapping("/importar-scraper")
    public ResponseEntity<Map<String, Object>> importarScraper() {
        List<Vaga> vagas = scraperService.executarScraping();
        return ResponseEntity.ok(Map.of(
                "message", "Scraping executado",
                "vagasImportadas", vagas.size()
        ));
    }

    @PostMapping("/importar-jooble")
    public ResponseEntity<Map<String, Object>> importarJooble(
            @RequestParam(defaultValue = "estágio tecnologia") String keywords,
            @RequestParam(defaultValue = "Mossoró, RN") String location) {
        List<Vaga> vagas = joobleService.buscarVagasJooble(keywords, location);
        return ResponseEntity.ok(Map.of(
                "message", "Importação Jooble concluída",
                "vagasImportadas", vagas.size()
        ));
    }

    @GetMapping("/vagas")
    public ResponseEntity<List<VagaDTO>> listarVagas() {
        return ResponseEntity.ok(adminService.listarVagas());
    }

    @PostMapping("/vagas")
    public ResponseEntity<VagaDTO> criarVaga(@RequestBody VagaDTO dto) {
        return ResponseEntity.ok(adminService.criarVaga(dto));
    }

    @PutMapping("/vagas/{id}")
    public ResponseEntity<VagaDTO> atualizarVaga(@PathVariable Long id,
            @RequestBody VagaDTO dto) {
        return ResponseEntity.ok(adminService.atualizarVaga(id, dto));
    }

    @DeleteMapping("/vagas/{id}")
    public ResponseEntity<Map<String, String>> deletarVaga(@PathVariable Long id) {
        adminService.deletarVaga(id);
        return ResponseEntity.ok(Map.of("message", "Vaga removida com sucesso"));
    }
}
