package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.model.Empresa;
import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.EmpresaRepository;
import br.com.unp.conectatech.repository.InteresseRepository;
import br.com.unp.conectatech.repository.VagaRepository;
import br.com.unp.conectatech.security.JwtUtil;
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
class EmpresaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private InteresseRepository interesseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private br.com.unp.conectatech.service.EmailService emailService;

    private String empresaToken;
    private Empresa empresa;
    private Vaga vaga;

    @BeforeEach
    void setUp() {
        interesseRepository.deleteAll();
        vagaRepository.deleteAll();
        empresaRepository.deleteAll();

        empresa = empresaRepository.save(Empresa.builder()
                .nome("Empresa Teste")
                .email("empresa@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .cnpj("12.345.678/0001-99")
                .telefone("84999990000")
                .emailVerificado(Boolean.TRUE)
                .build());

        empresaToken = jwtUtil.generateToken("empresa@email.com", "EMPRESA", "EMPRESA");

        vaga = vagaRepository.save(Vaga.builder()
                .titulo("Dev Java")
                .empresa("Empresa Teste")
                .descricao("Vaga de Java")
                .localizacao("Mossoro, RN")
                .url("https://example.com/1")
                .fonte(FonteVaga.EMPRESA)
                .empresaVinculada(empresa)
                .build());
    }

    // ==================== GET /api/empresa/jobs ====================

    @Test
    void listarVagas_comToken_retornaVagasDaEmpresa() throws Exception {
        mockMvc.perform(get("/api/empresa/jobs")
                        .header("Authorization", "Bearer " + empresaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Dev Java"));
    }

    @Test
    void listarVagas_semToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/empresa/jobs"))
                .andExpect(status().isForbidden());
    }

    // ==================== POST /api/empresa/jobs ====================

    @Test
    void criarVaga_comToken_retorna200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Nova Vaga",
                "empresa", "Empresa Teste",
                "descricao", "Descricao da vaga",
                "localizacao", "Natal, RN",
                "url", "https://example.com/nova"));

        mockMvc.perform(post("/api/empresa/jobs")
                        .header("Authorization", "Bearer " + empresaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Nova Vaga"))
                .andExpect(jsonPath("$.fonte").value("EMPRESA"));
    }

    @Test
    void criarVaga_semToken_retorna403() throws Exception {
        mockMvc.perform(post("/api/empresa/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ==================== PUT /api/empresa/jobs/{id} ====================

    @Test
    void atualizarVaga_proprietaria_retorna200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Vaga Atualizada",
                "empresa", "Empresa Teste",
                "url", "https://example.com/1"));

        mockMvc.perform(put("/api/empresa/jobs/" + vaga.getId())
                        .header("Authorization", "Bearer " + empresaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Vaga Atualizada"));
    }

    @Test
    void atualizarVaga_deOutraEmpresa_retorna403() throws Exception {
        Empresa outra = empresaRepository.save(Empresa.builder()
                .nome("Outra Empresa")
                .email("outra@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .cnpj("99.999.999/0001-99")
                .telefone("84888880000")
                .emailVerificado(Boolean.TRUE)
                .build());

        String outraToken = jwtUtil.generateToken("outra@email.com", "EMPRESA", "EMPRESA");

        mockMvc.perform(put("/api/empresa/jobs/" + vaga.getId())
                        .header("Authorization", "Bearer " + outraToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "titulo", "Hack", "empresa", "Hack", "url", "http://hack"))))
                .andExpect(status().isForbidden());
    }

    // ==================== DELETE /api/empresa/jobs/{id} ====================

    @Test
    void deletarVaga_proprietaria_retorna200() throws Exception {
        mockMvc.perform(delete("/api/empresa/jobs/" + vaga.getId())
                        .header("Authorization", "Bearer " + empresaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vaga removida com sucesso"));
    }

    @Test
    void deletarVaga_deOutraEmpresa_retorna403() throws Exception {
        empresaRepository.save(Empresa.builder()
                .nome("Outra")
                .email("outra2@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .cnpj("88.888.888/0001-88")
                .telefone("84777770000")
                .emailVerificado(Boolean.TRUE)
                .build());

        String outraToken = jwtUtil.generateToken("outra2@email.com", "EMPRESA", "EMPRESA");

        mockMvc.perform(delete("/api/empresa/jobs/" + vaga.getId())
                        .header("Authorization", "Bearer " + outraToken))
                .andExpect(status().isForbidden());
    }

    // ==================== GET /api/empresa/perfil ====================

    @Test
    void perfil_comToken_retornaDados() throws Exception {
        mockMvc.perform(get("/api/empresa/perfil")
                        .header("Authorization", "Bearer " + empresaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Empresa Teste"))
                .andExpect(jsonPath("$.email").value("empresa@email.com"))
                .andExpect(jsonPath("$.cnpj").value("12.345.678/0001-99"));
    }

    @Test
    void perfil_semToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/empresa/perfil"))
                .andExpect(status().isForbidden());
    }

    // ==================== PUT /api/empresa/perfil ====================

    @Test
    void atualizarPerfil_comToken_retorna200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Empresa Atualizada",
                "telefone", "84111110000",
                "areaAtuacao", "Tecnologia"));

        mockMvc.perform(put("/api/empresa/perfil")
                        .header("Authorization", "Bearer " + empresaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Empresa Atualizada"))
                .andExpect(jsonPath("$.areaAtuacao").value("Tecnologia"));
    }

    // ==================== GET /api/empresa/jobs/{id}/interessados ====================

    @Test
    void listarInteressados_vagaPropria_retorna200() throws Exception {
        mockMvc.perform(get("/api/empresa/jobs/" + vaga.getId() + "/interessados")
                        .header("Authorization", "Bearer " + empresaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listarInteressados_vagaDeOutra_retorna403() throws Exception {
        empresaRepository.save(Empresa.builder()
                .nome("Outra3")
                .email("outra3@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .cnpj("77.777.777/0001-77")
                .telefone("84666660000")
                .emailVerificado(Boolean.TRUE)
                .build());

        String outraToken = jwtUtil.generateToken("outra3@email.com", "EMPRESA", "EMPRESA");

        mockMvc.perform(get("/api/empresa/jobs/" + vaga.getId() + "/interessados")
                        .header("Authorization", "Bearer " + outraToken))
                .andExpect(status().isForbidden());
    }
}
