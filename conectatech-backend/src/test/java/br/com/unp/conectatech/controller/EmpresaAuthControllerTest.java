package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.repository.EmpresaRepository;
import br.com.unp.conectatech.repository.InteresseRepository;
import br.com.unp.conectatech.repository.VagaRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmpresaAuthControllerTest {

        // CNPJs válidos (dígitos verificadores corretos):
        // 11.222.333/0001-81 — empresa principal
        // 34.028.316/0001-03 — empresa secundária (email/cnpj duplicado)

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private InteresseRepository interesseRepository;

        @Autowired
        private VagaRepository vagaRepository;

        @Autowired
        private EmpresaRepository empresaRepository;

        @BeforeEach
        void setUp() {
                interesseRepository.deleteAll();
                vagaRepository.deleteAll();
                empresaRepository.deleteAll();
        }

        @Test
        void register_comDadosValidos_retorna200ComEmpresa() throws Exception {
                String body = objectMapper.writeValueAsString(Map.of(
                                "nome", "Tech Solutions LTDA",
                                "email", "contato@techsolutions.com",
                                "senha", "senha123",
                                "cnpj", "11.222.333/0001-81",
                                "telefone", "(84) 99999-0000"));

                mockMvc.perform(post("/api/empresa/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nome").value("Tech Solutions LTDA"))
                                .andExpect(jsonPath("$.cnpj").value("11.222.333/0001-81"))
                                .andExpect(jsonPath("$.email").value("contato@techsolutions.com"))
                                .andExpect(jsonPath("$.id").exists());
        }

        @Test
        void register_comEmailDuplicado_retorna400() throws Exception {
                String body = objectMapper.writeValueAsString(Map.of(
                                "nome", "Tech Solutions LTDA",
                                "email", "contato@techsolutions.com",
                                "senha", "senha123",
                                "cnpj", "11.222.333/0001-81",
                                "telefone", "(84) 99999-0000"));

                mockMvc.perform(post("/api/empresa/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body));

                String bodyDuplicado = objectMapper.writeValueAsString(Map.of(
                                "nome", "Outra Empresa",
                                "email", "contato@techsolutions.com",
                                "senha", "senha456",
                                "cnpj", "34.028.316/0001-03",
                                "telefone", "(84) 88888-0000"));

                mockMvc.perform(post("/api/empresa/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyDuplicado))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Email já cadastrado"));
        }

        @Test
        void register_comCnpjDuplicado_retorna400() throws Exception {
                String body = objectMapper.writeValueAsString(Map.of(
                                "nome", "Tech Solutions LTDA",
                                "email", "contato@techsolutions.com",
                                "senha", "senha123",
                                "cnpj", "11.222.333/0001-81",
                                "telefone", "(84) 99999-0000"));

                mockMvc.perform(post("/api/empresa/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body));

                String bodyDuplicado = objectMapper.writeValueAsString(Map.of(
                                "nome", "Outra Empresa",
                                "email", "outra@empresa.com",
                                "senha", "senha456",
                                "cnpj", "11.222.333/0001-81",
                                "telefone", "(84) 88888-0000"));

                mockMvc.perform(post("/api/empresa/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyDuplicado))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("CNPJ já cadastrado"));
        }

        @Test
        void register_comCnpjInvalido_retorna400() throws Exception {
                String body = objectMapper.writeValueAsString(Map.of(
                                "nome", "Tech Solutions LTDA",
                                "email", "contato@techsolutions.com",
                                "senha", "senha123",
                                "cnpj", "12.345.678/0001-99",
                                "telefone", "(84) 99999-0000"));

                mockMvc.perform(post("/api/empresa/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void register_semCamposObrigatorios_retorna400() throws Exception {
                String body = objectMapper.writeValueAsString(Map.of(
                                "email", "contato@techsolutions.com"));

                mockMvc.perform(post("/api/empresa/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void login_comCredenciaisValidas_retornaToken() throws Exception {
                mockMvc.perform(post("/api/empresa/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                                "nome", "Tech Solutions LTDA",
                                                "email", "contato@techsolutions.com",
                                                "senha", "senha123",
                                                "cnpj", "11.222.333/0001-81",
                                                "telefone", "(84) 99999-0000"))));

                String loginBody = objectMapper.writeValueAsString(Map.of(
                                "email", "contato@techsolutions.com",
                                "senha", "senha123"));

                mockMvc.perform(post("/api/empresa/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists())
                                .andExpect(jsonPath("$.nome").value("Tech Solutions LTDA"))
                                .andExpect(jsonPath("$.email").value("contato@techsolutions.com"))
                                .andExpect(jsonPath("$.cnpj").value("11.222.333/0001-81"));
        }

        @Test
        void login_comSenhaErrada_retorna400() throws Exception {
                mockMvc.perform(post("/api/empresa/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                                "nome", "Tech Solutions LTDA",
                                                "email", "contato@techsolutions.com",
                                                "senha", "senha123",
                                                "cnpj", "11.222.333/0001-81",
                                                "telefone", "(84) 99999-0000"))));

                String loginBody = objectMapper.writeValueAsString(Map.of(
                                "email", "contato@techsolutions.com",
                                "senha", "senhaerrada"));

                mockMvc.perform(post("/api/empresa/auth/login")
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

                mockMvc.perform(post("/api/empresa/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginBody))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"));
        }
}
