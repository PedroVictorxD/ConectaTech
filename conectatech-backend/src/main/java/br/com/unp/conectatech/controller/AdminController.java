package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.UsuarioDTO;
import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
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
