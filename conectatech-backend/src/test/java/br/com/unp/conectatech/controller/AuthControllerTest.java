package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

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
                .andExpect(jsonPath("$.curso").value("Ciencia da Computacao"))
                .andExpect(jsonPath("$.id").exists());
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
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "nome", "Maria",
                        "email", "maria@email.com",
                        "senha", "senha123"))));

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
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "nome", "Maria",
                        "email", "maria@email.com",
                        "senha", "senha123"))));

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
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "nome", "Ana",
                        "email", "ana@email.com",
                        "senha", "senha123"))));

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
}
