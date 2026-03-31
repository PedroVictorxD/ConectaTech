package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.model.Role;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.repository.UsuarioRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VagaSelecionadaRepository vagaSelecionadaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    private String studentToken;

    @BeforeEach
    void setUp() {
        vagaSelecionadaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario student = Usuario.builder()
                .nome("Aluno Teste")
                .email("aluno@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .curso("CC")
                .periodo("6")
                .role(Role.STUDENT)
                .build();
        usuarioRepository.save(student);

        var loginResponse = authService.login(
                new br.com.unp.conectatech.dto.LoginRequest() {{
                    setEmail("aluno@email.com");
                    setSenha("senha123");
                }});
        studentToken = loginResponse.getToken();
    }

    @Test
    void getProfile_comToken_retornaDados() throws Exception {
        mockMvc.perform(get("/api/profile")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Aluno Teste"))
                .andExpect(jsonPath("$.email").value("aluno@email.com"))
                .andExpect(jsonPath("$.curso").value("CC"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void getProfile_semToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProfile_comDadosValidos_retorna200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Aluno Atualizado",
                "curso", "Engenharia de Software",
                "periodo", "8"));

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Aluno Atualizado"))
                .andExpect(jsonPath("$.curso").value("Engenharia de Software"))
                .andExpect(jsonPath("$.periodo").value("8"));
    }

    @Test
    void updateProfile_semToken_retorna403() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Hacker",
                "curso", "Invasao"));

        mockMvc.perform(put("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProfile_semNome_retorna400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "curso", "CC",
                "periodo", "6"));

        mockMvc.perform(put("/api/profile")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
