package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.model.*;
import br.com.unp.conectatech.repository.*;
import br.com.unp.conectatech.security.JwtUtil;
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
        private VagaRepository vagaRepository;

        @Autowired
        private EmpresaRepository empresaRepository;

        @Autowired
        private UsuarioRepository usuarioRepository;

        @Autowired
        private VagaSelecionadaRepository vagaSelecionadaRepository;

        @Autowired
        private InteresseRepository interesseRepository;

        @Autowired
        private JwtUtil jwtUtil;

        private String redatorToken;
        private String studentToken;

        @BeforeEach
        void setUp() {
                interesseRepository.deleteAll();
                vagaSelecionadaRepository.deleteAll();
                vagaRepository.deleteAll();
                empresaRepository.deleteAll();
                usuarioRepository.deleteAll();

                Usuario redator = Usuario.builder()
                                .nome("Redator Teste")
                                .email("redator@email.com")
                                .senha("senha123")
                                .role(Role.REDATOR)
                                .build();
                usuarioRepository.save(redator);
                redatorToken = jwtUtil.generateToken(redator.getEmail(), Role.REDATOR.name());

                Usuario student = Usuario.builder()
                                .nome("Estudante Teste")
                                .email("student@email.com")
                                .senha("senha123")
                                .role(Role.STUDENT)
                                .build();
                usuarioRepository.save(student);
                studentToken = jwtUtil.generateToken(student.getEmail(), Role.STUDENT.name());
        }

        @Test
        void listarVagas_comRedator_retorna200() throws Exception {
                vagaRepository.save(Vaga.builder()
                                .titulo("Dev Java")
                                .empresa("Empresa X")
                                .url("https://example.com/1")
                                .fonte(FonteVaga.ADMIN)
                                .build());

                mockMvc.perform(get("/api/redator/jobs")
                                .header("Authorization", "Bearer " + redatorToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].titulo").value("Dev Java"));
        }

        @Test
        void listarVagas_comStudent_retorna403() throws Exception {
                mockMvc.perform(get("/api/redator/jobs")
                                .header("Authorization", "Bearer " + studentToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        void criarVaga_comRedator_retorna200ComFonteAdmin() throws Exception {
                String body = objectMapper.writeValueAsString(Map.of(
                                "titulo", "Estagio Backend",
                                "empresa", "Tech Corp",
                                "url", "https://example.com/vaga"));

                mockMvc.perform(post("/api/redator/jobs")
                                .header("Authorization", "Bearer " + redatorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.titulo").value("Estagio Backend"))
                                .andExpect(jsonPath("$.empresa").value("Tech Corp"))
                                .andExpect(jsonPath("$.fonte").value("ADMIN"));
        }

        @Test
        void atualizarVaga_comRedator_retorna200() throws Exception {
                Vaga vaga = vagaRepository.save(Vaga.builder()
                                .titulo("Vaga Original")
                                .empresa("Empresa A")
                                .url("https://example.com/original")
                                .fonte(FonteVaga.ADMIN)
                                .build());

                String body = objectMapper.writeValueAsString(Map.of(
                                "titulo", "Vaga Atualizada",
                                "empresa", "Empresa B",
                                "url", "https://example.com/atualizada"));

                mockMvc.perform(put("/api/redator/jobs/" + vaga.getId())
                                .header("Authorization", "Bearer " + redatorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.titulo").value("Vaga Atualizada"))
                                .andExpect(jsonPath("$.empresa").value("Empresa B"));
        }

        @Test
        void deletarVaga_comRedator_retorna200() throws Exception {
                Vaga vaga = vagaRepository.save(Vaga.builder()
                                .titulo("Vaga para Deletar")
                                .empresa("Empresa X")
                                .url("https://example.com/delete")
                                .fonte(FonteVaga.ADMIN)
                                .build());

                mockMvc.perform(delete("/api/redator/jobs/" + vaga.getId())
                                .header("Authorization", "Bearer " + redatorToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Vaga removida com sucesso"));
        }

        @Test
        void listarEmpresas_comRedator_retorna200() throws Exception {
                empresaRepository.save(Empresa.builder()
                                .nome("Empresa Teste")
                                .email("empresa@test.com")
                                .senha("senha123")
                                .cnpj("11.222.333/0001-81")
                                .telefone("(84) 99999-0000")
                                .build());

                mockMvc.perform(get("/api/redator/empresas")
                                .header("Authorization", "Bearer " + redatorToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].nome").value("Empresa Teste"));
        }

        @Test
        void criarEmpresa_comRedator_retorna200() throws Exception {
                String body = objectMapper.writeValueAsString(Map.of(
                                "nome", "Nova Empresa",
                                "email", "nova@empresa.com",
                                "senha", "senha123",
                                "cnpj", "34.028.316/0001-03",
                                "telefone", "(84) 88888-0000"));

                mockMvc.perform(post("/api/redator/empresas")
                                .header("Authorization", "Bearer " + redatorToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nome").value("Nova Empresa"))
                                .andExpect(jsonPath("$.email").value("nova@empresa.com"));
        }

        @Test
        void deletarEmpresa_comRedator_retorna200() throws Exception {
                Empresa empresa = empresaRepository.save(Empresa.builder()
                                .nome("Empresa Deletar")
                                .email("deletar@empresa.com")
                                .senha("senha123")
                                .cnpj("57.755.227/0001-29")
                                .telefone("(84) 77777-0000")
                                .build());

                mockMvc.perform(delete("/api/redator/empresas/" + empresa.getId())
                                .header("Authorization", "Bearer " + redatorToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Empresa removida com sucesso"));
        }
}
