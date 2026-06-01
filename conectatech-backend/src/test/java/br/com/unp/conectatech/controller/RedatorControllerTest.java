package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.model.Empresa;
import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Role;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.EmpresaRepository;
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
import org.springframework.boot.test.mock.mockito.MockBean;
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
class RedatorControllerTest {

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
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @MockBean
    private br.com.unp.conectatech.service.EmailService emailService;

    private String redatorToken;
    private String studentToken;
    private Vaga vaga;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        vagaSelecionadaRepository.deleteAll();
        vagaRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario redator = Usuario.builder()
                .nome("Redator")
                .email("redator@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .role(Role.REDATOR)
                .emailVerificado(Boolean.TRUE)
                .build();
        usuarioRepository.save(redator);

        Usuario student = Usuario.builder()
                .nome("Aluno")
                .email("aluno@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .role(Role.STUDENT)
                .emailVerificado(Boolean.TRUE)
                .build();
        usuarioRepository.save(student);

        redatorToken = authService.login(
                new br.com.unp.conectatech.dto.LoginRequest() {{
                    setEmail("redator@email.com");
                    setSenha("senha123");
                }}).getToken();

        studentToken = authService.login(
                new br.com.unp.conectatech.dto.LoginRequest() {{
                    setEmail("aluno@email.com");
                    setSenha("senha123");
                }}).getToken();

        vaga = vagaRepository.save(Vaga.builder()
                .titulo("Vaga Teste")
                .empresa("Empresa Teste")
                .descricao("Descricao")
                .localizacao("Mossoro")
                .url("https://example.com/1")
                .fonte(FonteVaga.ADMIN)
                .build());

        empresa = empresaRepository.save(Empresa.builder()
                .nome("Empresa Teste")
                .email("empresa@teste.com")
                .senha(passwordEncoder.encode("senha123"))
                .cnpj("12.345.678/0001-99")
                .telefone("84999990000")
                .emailVerificado(Boolean.TRUE)
                .build());
    }

    // ==================== VAGAS ====================

    @Test
    void listarVagas_comoRedator_retorna200() throws Exception {
        mockMvc.perform(get("/api/redator/jobs")
                        .header("Authorization", "Bearer " + redatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Vaga Teste"));
    }

    @Test
    void listarVagas_comoStudent_retorna403() throws Exception {
        mockMvc.perform(get("/api/redator/jobs")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void criarVaga_comoRedator_retorna200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Nova Vaga",
                "empresa", "Nova Empresa",
                "descricao", "Desc",
                "localizacao", "Natal",
                "url", "https://example.com/nova"));

        mockMvc.perform(post("/api/redator/jobs")
                        .header("Authorization", "Bearer " + redatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Nova Vaga"))
                .andExpect(jsonPath("$.fonte").value("ADMIN"));
    }

    @Test
    void atualizarVaga_comoRedator_retorna200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Atualizada",
                "empresa", "Empresa",
                "url", "https://example.com/1"));

        mockMvc.perform(put("/api/redator/jobs/" + vaga.getId())
                        .header("Authorization", "Bearer " + redatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Atualizada"));
    }

    @Test
    void deletarVaga_comoRedator_retorna200() throws Exception {
        mockMvc.perform(delete("/api/redator/jobs/" + vaga.getId())
                        .header("Authorization", "Bearer " + redatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vaga removida com sucesso"));
    }

    @Test
    void deletarVaga_comoStudent_retorna403() throws Exception {
        mockMvc.perform(delete("/api/redator/jobs/" + vaga.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    // ==================== EMPRESAS ====================

    @Test
    void listarEmpresas_comoRedator_retorna200() throws Exception {
        mockMvc.perform(get("/api/redator/empresas")
                        .header("Authorization", "Bearer " + redatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Empresa Teste"));
    }

    @Test
    void buscarEmpresa_comoRedator_retorna200() throws Exception {
        mockMvc.perform(get("/api/redator/empresas/" + empresa.getId())
                        .header("Authorization", "Bearer " + redatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("empresa@teste.com"));
    }

    @Test
    void buscarEmpresa_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/redator/empresas/99999")
                        .header("Authorization", "Bearer " + redatorToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void criarEmpresa_comoRedator_retorna200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Nova Empresa",
                "email", "nova@empresa.com",
                "senha", "senha123",
                "cnpj", "99.999.999/0001-99",
                "telefone", "84888880000"));

        mockMvc.perform(post("/api/redator/empresas")
                        .header("Authorization", "Bearer " + redatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nova Empresa"));
    }

    @Test
    void atualizarEmpresa_comoRedator_retorna200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Empresa Atualizada",
                "telefone", "84111110000"));

        mockMvc.perform(put("/api/redator/empresas/" + empresa.getId())
                        .header("Authorization", "Bearer " + redatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Empresa Atualizada"));
    }

    @Test
    void deletarEmpresa_comoRedator_retorna200() throws Exception {
        mockMvc.perform(delete("/api/redator/empresas/" + empresa.getId())
                        .header("Authorization", "Bearer " + redatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Empresa removida com sucesso"));
    }

    @Test
    void deletarEmpresa_comoStudent_retorna403() throws Exception {
        mockMvc.perform(delete("/api/redator/empresas/" + empresa.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }
}
