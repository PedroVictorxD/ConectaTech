package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.model.Empresa;
import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Role;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.*;
import br.com.unp.conectatech.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InteresseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private InteresseRepository interesseRepository;

    @Autowired
    private VagaSelecionadaRepository vagaSelecionadaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @MockBean
    private br.com.unp.conectatech.service.EmailService emailService;

    private String studentToken;
    private Vaga vaga;

    @BeforeEach
    void setUp() {
        interesseRepository.deleteAll();
        vagaSelecionadaRepository.deleteAll();
        vagaRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario student = Usuario.builder()
                .nome("Aluno Teste")
                .email("aluno@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .curso("CC")
                .role(Role.STUDENT)
                .emailVerificado(Boolean.TRUE)
                .build();
        usuarioRepository.save(student);

        studentToken = authService.login(
                new br.com.unp.conectatech.dto.LoginRequest() {{
                    setEmail("aluno@email.com");
                    setSenha("senha123");
                }}).getToken();

        Empresa empresa = empresaRepository.save(Empresa.builder()
                .nome("Empresa Teste")
                .email("empresa@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .cnpj("12.345.678/0001-99")
                .telefone("84999990000")
                .emailVerificado(Boolean.TRUE)
                .build());

        vaga = vagaRepository.save(Vaga.builder()
                .titulo("Vaga Estagio")
                .empresa("Empresa Teste")
                .descricao("Estagio em TI")
                .localizacao("Mossoro, RN")
                .url("https://example.com/1")
                .fonte(FonteVaga.EMPRESA)
                .empresaVinculada(empresa)
                .build());
    }

    // ==================== POST /api/jobs/{id}/interesse ====================

    @Test
    void demonstrarInteresse_comToken_retorna200() throws Exception {
        mockMvc.perform(post("/api/jobs/" + vaga.getId() + "/interesse")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Interesse demonstrado com sucesso"));
    }

    @Test
    void demonstrarInteresse_semToken_retorna403() throws Exception {
        mockMvc.perform(post("/api/jobs/" + vaga.getId() + "/interesse"))
                .andExpect(status().isForbidden());
    }

    @Test
    void demonstrarInteresse_duplicado_retorna400() throws Exception {
        mockMvc.perform(post("/api/jobs/" + vaga.getId() + "/interesse")
                .header("Authorization", "Bearer " + studentToken));

        mockMvc.perform(post("/api/jobs/" + vaga.getId() + "/interesse")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Interesse já demonstrado nesta vaga"));
    }

    @Test
    void demonstrarInteresse_vagaInexistente_retorna404() throws Exception {
        mockMvc.perform(post("/api/jobs/99999/interesse")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNotFound());
    }

    // ==================== DELETE /api/jobs/{id}/interesse ====================

    @Test
    void removerInteresse_existente_retorna200() throws Exception {
        mockMvc.perform(post("/api/jobs/" + vaga.getId() + "/interesse")
                .header("Authorization", "Bearer " + studentToken));

        mockMvc.perform(delete("/api/jobs/" + vaga.getId() + "/interesse")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Interesse removido com sucesso"));
    }

    @Test
    void removerInteresse_inexistente_retorna404() throws Exception {
        mockMvc.perform(delete("/api/jobs/" + vaga.getId() + "/interesse")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void removerInteresse_semToken_retorna403() throws Exception {
        mockMvc.perform(delete("/api/jobs/" + vaga.getId() + "/interesse"))
                .andExpect(status().isForbidden());
    }
}
