package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.service.VagaSelecionadaService;
import br.com.unp.conectatech.service.VagaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vagas")
public class VagaController {

    private final VagaService vagaService;
    private final VagaSelecionadaService vagaSelecionadaService;

    public VagaController(VagaService vagaService, VagaSelecionadaService vagaSelecionadaService) {
        this.vagaService = vagaService;
        this.vagaSelecionadaService = vagaSelecionadaService;
    }

    @GetMapping
    public ResponseEntity<List<VagaDTO>> listarTodas(@RequestParam(required = false) String busca) {
        List<VagaDTO> vagas;
        if (busca != null && !busca.isBlank()) {
            vagas = vagaService.buscarPorTitulo(busca);
        } else {
            vagas = vagaService.listarTodas();
        }
        return ResponseEntity.ok(vagas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VagaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vagaService.buscarPorId(id));
    }

    @PostMapping("/selecionar")
    public ResponseEntity<Map<String, String>> selecionar(@RequestBody Map<String, Long> body,
            Authentication authentication) {
        String email = authentication.getName();
        Long vagaId = body.get("vagaId");
        vagaSelecionadaService.selecionar(email, vagaId);
        return ResponseEntity.ok(Map.of("message", "Vaga selecionada com sucesso"));
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<VagaDTO>> minhasVagas(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(vagaSelecionadaService.listarMinhasVagas(email));
    }

    @DeleteMapping("/minhas/{vagaId}")
    public ResponseEntity<Map<String, String>> removerSelecionada(@PathVariable Long vagaId,
            Authentication authentication) {
        String email = authentication.getName();
        vagaSelecionadaService.removerSelecionada(email, vagaId);
        return ResponseEntity.ok(Map.of("message", "Vaga removida das selecionadas"));
    }
}
