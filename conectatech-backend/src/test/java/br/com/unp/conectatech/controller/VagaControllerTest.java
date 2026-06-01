package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.dto.LoginResponse;
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
class VagaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VagaSelecionadaRepository vagaSelecionadaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    private String studentToken;
    private Vaga vaga;

    @BeforeEach
    void setUp() {
        vagaSelecionadaRepository.deleteAll();
        vagaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario student = Usuario.builder()
                .nome("Aluno Teste")
                .email("aluno@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .curso("CC")
                .periodo("6")
                .role(Role.STUDENT)
                .emailVerificado(Boolean.TRUE)
                .build();
        usuarioRepository.save(student);

        var loginResponse = authService.login(
                new br.com.unp.conectatech.dto.LoginRequest() {{
                    setEmail("aluno@email.com");
                    setSenha("senha123");
                }});
        studentToken = loginResponse.getToken();

        vaga = Vaga.builder()
                .titulo("Estagiario Dev Web")
                .empresa("Tech LTDA")
                .descricao("Vaga de estagio")
                .localizacao("Mossoro, RN")
                .url("https://example.com/vaga/1")
                .fonte(FonteVaga.ADMIN)
                .build();
        vaga = vagaRepository.save(vaga);
    }

    @Test
    void listarVagas_semAutenticacao_retorna200() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Estagiario Dev Web"));
    }

    @Test
    void buscarPorId_vagaExistente_retorna200() throws Exception {
        mockMvc.perform(get("/api/jobs/" + vaga.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Estagiario Dev Web"))
                .andExpect(jsonPath("$.empresa").value("Tech LTDA"));
    }

    @Test
    void buscarPorId_vagaInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/jobs/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarVagas_comFiltroBusca_retornaFiltrado() throws Exception {
        Vaga outra = Vaga.builder()
                .titulo("Analista RH")
                .empresa("Empresa RH")
                .descricao("Vaga RH")
                .localizacao("Natal, RN")
                .url("https://example.com/vaga/2")
                .fonte(FonteVaga.ADMIN)
                .build();
        vagaRepository.save(outra);

        mockMvc.perform(get("/api/jobs").param("busca", "Dev"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Estagiario Dev Web"));
    }

    @Test
    void selecionarVaga_comToken_retorna200() throws Exception {
        mockMvc.perform(post("/api/jobs/select")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("vagaId", vaga.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vaga selecionada com sucesso"));
    }

    @Test
    void selecionarVaga_semToken_retorna403() throws Exception {
        mockMvc.perform(post("/api/jobs/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("vagaId", vaga.getId()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void selecionarVaga_duplicada_retorna400() throws Exception {
        mockMvc.perform(post("/api/jobs/select")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("vagaId", vaga.getId()))));

        mockMvc.perform(post("/api/jobs/select")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("vagaId", vaga.getId()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Vaga já selecionada"));
    }

    @Test
    void minhasVagas_comToken_retornaLista() throws Exception {
        mockMvc.perform(post("/api/jobs/select")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("vagaId", vaga.getId()))));

        mockMvc.perform(get("/api/jobs/my-selections")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Estagiario Dev Web"));
    }

    @Test
    void minhasVagas_semToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/jobs/my-selections"))
                .andExpect(status().isForbidden());
    }

    @Test
    void removerSelecionada_comToken_retorna200() throws Exception {
        mockMvc.perform(post("/api/jobs/select")
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("vagaId", vaga.getId()))));

        mockMvc.perform(delete("/api/jobs/my-selections/" + vaga.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vaga removida das selecionadas"));

        mockMvc.perform(get("/api/jobs/my-selections")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void removerSelecionada_semToken_retorna403() throws Exception {
        mockMvc.perform(delete("/api/jobs/my-selections/" + vaga.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void removerSelecionada_naoSelecionada_retorna404() throws Exception {
        mockMvc.perform(delete("/api/jobs/my-selections/" + vaga.getId())
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNotFound());
    }
}
