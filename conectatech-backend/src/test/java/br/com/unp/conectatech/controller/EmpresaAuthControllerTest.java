package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.model.Empresa;
import br.com.unp.conectatech.repository.EmpresaRepository;
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

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmpresaAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private br.com.unp.conectatech.service.EmailService emailService;

    @BeforeEach
    void setUp() {
        empresaRepository.deleteAll();
    }

    @Test
    void register_comDadosValidos_retorna200ComEmpresa() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Empresa Teste",
                "email", "empresa@email.com",
                "senha", "senha123",
                "cnpj", "12.345.678/0001-99",
                "telefone", "84999990000"));

        mockMvc.perform(post("/api/empresa/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Empresa Teste"))
                .andExpect(jsonPath("$.email").value("empresa@email.com"))
                .andExpect(jsonPath("$.emailVerificado").value(false))
                .andExpect(jsonPath("$.id").exists());

        String token = empresaRepository.findByEmail("empresa@email.com").orElseThrow().getTokenConfirmacaoEmail();
        verify(emailService).enviarConfirmacaoEmail("empresa@email.com", "Empresa Teste", token, "empresa");
    }

    @Test
    void login_comCredenciaisValidas_retornaToken() throws Exception {
        salvarEmpresa("empresa@email.com", "senha123", true);

        mockMvc.perform(post("/api/empresa/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "empresa@email.com",
                                "senha", "senha123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("empresa@email.com"));
    }

    @Test
    void forgotPassword_comEmailExistente_retorna200() throws Exception {
        salvarEmpresa("empresa@email.com", "senha123", true);

        mockMvc.perform(post("/api/empresa/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "empresa@email.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email de recuperacao enviado com sucesso"));
    }

    @Test
    void forgotPassword_comEmailInexistente_retorna404() throws Exception {
        mockMvc.perform(post("/api/empresa/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "naoexiste@email.com"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void resetPassword_comTokenValido_retorna200() throws Exception {
        Empresa empresa = salvarEmpresa("empresa@email.com", "senha123", true);
        empresa.setTokenRecuperacao("token-valido");
        empresa.setTokenExpiracao(LocalDateTime.now().plusHours(1));
        empresaRepository.save(empresa);

        mockMvc.perform(put("/api/empresa/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "token-valido",
                                "novaSenha", "novasenha456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha alterada com sucesso"));

        mockMvc.perform(post("/api/empresa/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "empresa@email.com",
                                "senha", "novasenha456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void resetPassword_comTokenInvalido_retorna400() throws Exception {
        mockMvc.perform(put("/api/empresa/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "token-invalido",
                                "novaSenha", "novasenha456"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_comTokenExpirado_retorna400() throws Exception {
        Empresa empresa = salvarEmpresa("empresa@email.com", "senha123", true);
        empresa.setTokenRecuperacao("token-expirado");
        empresa.setTokenExpiracao(LocalDateTime.now().minusMinutes(1));
        empresaRepository.save(empresa);

        mockMvc.perform(put("/api/empresa/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                                "token", "token-expirado",
                                "novaSenha", "novasenha456"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_comEmailNaoConfirmado_retorna400() throws Exception {
        salvarEmpresa("empresa@email.com", "senha123", false);

        mockMvc.perform(post("/api/empresa/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "empresa@email.com",
                                "senha", "senha123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Confirme seu email antes de entrar"));
    }

    @Test
    void confirmEmail_comTokenValido_retorna200() throws Exception {
        mockMvc.perform(post("/api/empresa/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "nome", "Empresa Teste",
                        "email", "empresa@email.com",
                        "senha", "senha123",
                        "cnpj", "12.345.678/0001-99",
                        "telefone", "84999990000"))));

        String token = empresaRepository.findByEmail("empresa@email.com").orElseThrow().getTokenConfirmacaoEmail();

        mockMvc.perform(post("/api/empresa/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", token))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email confirmado com sucesso"));
    }

    @Test
    void confirmEmail_comTokenInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/empresa/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", "token-invalido"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token de confirmação inválido"));
    }

    @Test
    void confirmEmail_comTokenExpirado_retorna400() throws Exception {
        mockMvc.perform(post("/api/empresa/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "nome", "Empresa Teste",
                        "email", "empresa@email.com",
                        "senha", "senha123",
                        "cnpj", "12.345.678/0001-99",
                        "telefone", "84999990000"))));

        var empresa = empresaRepository.findByEmail("empresa@email.com").orElseThrow();
        empresa.setTokenConfirmacaoExpiracao(empresa.getCriadoEm().minusMinutes(1));
        empresaRepository.save(empresa);

        mockMvc.perform(post("/api/empresa/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("token", empresa.getTokenConfirmacaoEmail()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token de confirmação expirado"));
    }

    @Test
    void confirmEmail_semToken_retorna400() throws Exception {
        mockMvc.perform(post("/api/empresa/auth/confirm-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.token").value("Token é obrigatório"));
    }

    private Empresa salvarEmpresa(String email, String senha, boolean emailVerificado) {
        return empresaRepository.save(Empresa.builder()
                .nome("Empresa Teste")
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .cnpj("12.345.678/0001-99")
                .telefone("84999990000")
                .emailVerificado(emailVerificado)
                .build());
    }
}
