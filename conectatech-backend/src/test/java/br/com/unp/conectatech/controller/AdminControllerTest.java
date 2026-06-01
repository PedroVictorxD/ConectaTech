package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Role;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.UsuarioRepository;
import br.com.unp.conectatech.repository.VagaRepository;
import br.com.unp.conectatech.repository.VagaSelecionadaRepository;
import br.com.unp.conectatech.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private VagaSelecionadaRepository vagaSelecionadaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    private String adminToken;
    private String studentToken;
    private Vaga vaga;
    private Usuario admin;

    @BeforeEach
    void setUp() {
        vagaSelecionadaRepository.deleteAll();
        vagaRepository.deleteAll();
        usuarioRepository.deleteAll();

        admin = Usuario.builder()
                .nome("Admin")
                .email("admin@email.com")
                .senha(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .emailVerificado(Boolean.TRUE)
                .build();
        usuarioRepository.save(admin);

        Usuario student = Usuario.builder()
                .nome("Aluno")
                .email("aluno@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .curso("CC")
                .periodo("6")
                .role(Role.STUDENT)
                .emailVerificado(Boolean.TRUE)
                .build();
        usuarioRepository.save(student);

        adminToken = authService.login(
                new br.com.unp.conectatech.dto.LoginRequest() {{
                    setEmail("admin@email.com");
                    setSenha("admin123");
                }}).getToken();

        studentToken = authService.login(
                new br.com.unp.conectatech.dto.LoginRequest() {{
                    setEmail("aluno@email.com");
                    setSenha("senha123");
                }}).getToken();

        vaga = Vaga.builder()
                .titulo("Vaga Teste")
                .empresa("Empresa Teste")
                .descricao("Descricao")
                .localizacao("Mossoro")
                .url("https://example.com/1")
                .fonte(FonteVaga.ADMIN)
                .build();
        vaga = vagaRepository.save(vaga);
    }

    @Test
    void listarUsuarios_comoAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void listarUsuarios_comoStudent_retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarUsuarios_semToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void buscarUsuarioPorId_comoAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/admin/users/" + admin.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Admin"))
                .andExpect(jsonPath("$.email").value("admin@email.com"));
    }

    @Test
    void buscarUsuarioPorId_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/admin/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void atualizarUsuario_comoAdmin_retorna200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Admin Atualizado",
                "role", "ADMIN"));

        mockMvc.perform(put("/api/admin/users/" + admin.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Admin Atualizado"));
    }

    @Test
    void listarVagas_comoAdmin_retorna200() throws Exception {
        mockMvc.perform(get("/api/admin/jobs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Vaga Teste"));
    }

    @Test
    void criarVaga_comoAdmin_retorna200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Nova Vaga",
                "empresa", "Nova Empresa",
                "descricao", "Descricao da vaga",
                "localizacao", "Natal, RN",
                "url", "https://example.com/nova"));

        mockMvc.perform(post("/api/admin/jobs")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Nova Vaga"))
                .andExpect(jsonPath("$.fonte").value("ADMIN"));
    }

    @Test
    void criarVaga_comoStudent_retorna403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Vaga Hacker",
                "empresa", "Hacker Inc"));

        mockMvc.perform(post("/api/admin/jobs")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void atualizarVaga_comoAdmin_retorna200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Vaga Atualizada",
                "empresa", "Empresa Atualizada",
                "url", "https://example.com/1"));

        mockMvc.perform(put("/api/admin/jobs/" + vaga.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Vaga Atualizada"));
    }

    @Test
    void deletarVaga_comoAdmin_retorna200() throws Exception {
        mockMvc.perform(delete("/api/admin/jobs/" + vaga.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vaga removida com sucesso"));

        mockMvc.perform(get("/api/admin/jobs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deletarVaga_comoStudent_retorna403() throws Exception {
        mockMvc.perform(delete("/api/admin/jobs/" + vaga.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletarVaga_inexistente_retorna404() throws Exception {
        mockMvc.perform(delete("/api/admin/jobs/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
