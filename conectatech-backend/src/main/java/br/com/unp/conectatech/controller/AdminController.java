package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.InteresseDTO;
import br.com.unp.conectatech.dto.MessageResponse;
import br.com.unp.conectatech.dto.UsuarioDTO;
import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.service.AdminService;
import br.com.unp.conectatech.service.InteresseService;
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
    private final InteresseService interesseService;

    public AdminController(AdminService adminService, InteresseService interesseService) {
        this.adminService = adminService;
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
