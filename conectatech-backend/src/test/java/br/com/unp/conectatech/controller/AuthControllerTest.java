package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @MockBean
    private br.com.unp.conectatech.service.EmailService emailService;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    void register_comDadosValidos_retorna200ComUsuario() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Joao Silva",
                "email", "joao@email.com",
                "senha", "senha123",
                "curso", "Ciencia da Computacao",
                "periodo", "6"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joao Silva"))
                .andExpect(jsonPath("$.email").value("joao@email.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.emailVerificado").value(false))
                .andExpect(jsonPath("$.curso").value("Ciencia da Computacao"))
                .andExpect(jsonPath("$.id").exists());

        String token = usuarioRepository.findByEmail("joao@email.com").orElseThrow().getTokenConfirmacaoEmail();
        verify(emailService).enviarConfirmacaoEmail("joao@email.com", "Joao Silva", token, "aluno");
    }

    @Test
    void register_comEmailDuplicado_retorna400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Joao Silva",
                "email", "joao@email.com",
                "senha", "senha123"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email já cadastrado"));
    }

    @Test
    void register_semNome_retorna400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "joao@email.com",
                "senha", "senha123"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_comEmailInvalido_retorna400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Joao",
                "email", "nao-eh-email",
                "senha", "senha123"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_comSenhaCurta_retorna400() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Joao",
                "email", "joao@email.com",
                "senha", "123"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_comCredenciaisValidas_retornaTokenEDados() throws Exception {
        salvarUsuarioConfirmado("Maria", "maria@email.com", "senha123");

        String loginBody = objectMapper.writeValueAsString(Map.of(
                "email", "maria@email.com",
                "senha", "senha123"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.nome").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void login_comSenhaErrada_retorna400() throws Exception {
        salvarUsuarioConfirmado("Maria", "maria@email.com", "senha123");

        String loginBody = objectMapper.writeValueAsString(Map.of(
                "email", "maria@email.com",
                "senha", "senhaerrada"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"));
    }

    @Test
    void login_comEmailNaoConfirmado_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "nome", "Maria",
                        "email", "maria@email.com",
                        "senha", "senha123"))));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "maria@email.com",
                                "senha", "senha123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Confirme seu email antes de entrar"));
    }

    @Test
    void login_comEmailInexistente_retorna400() throws Exception {
        String loginBody = objectMapper.writeValueAsString(Map.of(
                "email", "naoexiste@email.com",
                "senha", "senha123"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"));
    }

    @Test
    void forgotPassword_comEmailExistente_retorna200() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "nome", "Pedro",
                        "email", "pedro@email.com",
                        "senha", "senha123"))));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "pedro@email.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void forgotPassword_comEmailInexistente_retorna404() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "naoexiste@email.com"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void resetPassword_comTokenValido_retorna200() throws Exception {
        salvarUsuarioConfirmado("Ana", "ana@email.com", "senha123");

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "ana@email.com"))));

        String token = usuarioRepository.findByEmail("ana@email.com").get().getTokenRecuperacao();

        mockMvc.perform(put("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token,
                                "novaSenha", "novasenha456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha alterada com sucesso"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "ana@email.com",
                                "senha", "novasenha456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void resetPassword_comTokenInvalido_retorna400() throws Exception {
        mockMvc.perform(put("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "token-invalido",
                                "novaSenha", "novasenha456"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void confirmEmail_comTokenValido_retorna200() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "nome", "Joao",
                        "email", "joao@email.com",
                        "senha", "senha123"))));

        String token = usuarioRepository.findByEmail("joao@email.com").orElseThrow().getTokenConfirmacaoEmail();

        mockMvc.perform(post("/api/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", token))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email confirmado com sucesso"));
    }

    @Test
    void confirmEmail_comTokenInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", "token-invalido"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token de confirmação inválido"));
    }

    @Test
    void confirmEmail_comTokenExpirado_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "nome", "Joao",
                        "email", "joao@email.com",
                        "senha", "senha123"))));

        var usuario = usuarioRepository.findByEmail("joao@email.com").orElseThrow();
        usuario.setTokenConfirmacaoExpiracao(usuario.getCriadoEm().minusMinutes(1));
        usuarioRepository.save(usuario);

        mockMvc.perform(post("/api/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", usuario.getTokenConfirmacaoEmail()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token de confirmação expirado"));
    }

    @Test
    void confirmEmail_semToken_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.token").value("Token é obrigatório"));
    }

    private void salvarUsuarioConfirmado(String nome, String email, String senha) {
        var usuario = br.com.unp.conectatech.model.Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(senha))
                .role(br.com.unp.conectatech.model.Role.STUDENT)
                .emailVerificado(Boolean.TRUE)
                .build();
        usuarioRepository.save(usuario);
    }
}
