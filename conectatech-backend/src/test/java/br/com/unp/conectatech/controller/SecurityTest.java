package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.model.Role;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.repository.UsuarioRepository;
import br.com.unp.conectatech.repository.VagaSelecionadaRepository;
import br.com.unp.conectatech.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

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
                .nome("Aluno")
                .email("aluno@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .role(Role.STUDENT)
                .emailVerificado(Boolean.TRUE)
                .build();
        usuarioRepository.save(student);

        studentToken = authService.login(
                new br.com.unp.conectatech.dto.LoginRequest() {{
                    setEmail("aluno@email.com");
                    setSenha("senha123");
                }}).getToken();
    }

    @Test
    void endpointsPublicos_GET_acessiveisSemToken() throws Exception {
        mockMvc.perform(get("/api/jobs")).andExpect(status().isOk());
    }

    @Test
    void endpointsAuth_naoExigemToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void endpointsAdmin_inacessiveisParaStudent() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/jobs")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointsAdmin_inacessiveisSemToken() throws Exception {
        mockMvc.perform(get("/api/admin/users")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/jobs")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/admin/jobs/1")).andExpect(status().isForbidden());
    }

    @Test
    void endpointsAutenticados_inacessiveisSemToken() throws Exception {
        mockMvc.perform(get("/api/profile")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/jobs/my-selections")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/jobs/select")).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/jobs/my-selections/1")).andExpect(status().isForbidden());
    }

    @Test
    void tokenInvalido_retorna403() throws Exception {
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "Bearer token-invalido-qualquer"))
                .andExpect(status().isForbidden());
    }

    @Test
    void tokenExpirado_formatoErrado_retorna403() throws Exception {
        mockMvc.perform(get("/api/profile")
                .header("Authorization", "InvalidFormat"))
                .andExpect(status().isForbidden());
    }

    @Test
    void swaggerUI_acessivelSemToken() throws Exception {
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }
}
