package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.model.Empresa;
import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.EmpresaRepository;
import br.com.unp.conectatech.repository.InteresseRepository;
import br.com.unp.conectatech.repository.UsuarioRepository;
import br.com.unp.conectatech.repository.VagaRepository;
import br.com.unp.conectatech.repository.VagaSelecionadaRepository;
import br.com.unp.conectatech.security.JwtUtil;
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
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VagaSelecionadaRepository vagaSelecionadaRepository;

    @Autowired
    private InteresseRepository interesseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private Empresa empresa;
    private Empresa outraEmpresa;
    private String empresaToken;
    private String outraEmpresaToken;
    private Vaga vaga;

    @BeforeEach
    void setUp() {
        interesseRepository.deleteAll();
        vagaSelecionadaRepository.deleteAll();
        vagaRepository.deleteAll();
        empresaRepository.deleteAll();
        usuarioRepository.deleteAll();

        empresa = Empresa.builder()
                .nome("Tech Corp")
                .email("tech@corp.com")
                .senha(passwordEncoder.encode("senha123"))
                .cnpj("12.345.678/0001-99")
                .telefone("(84)99999-0000")
                .build();
        empresa = empresaRepository.save(empresa);

        outraEmpresa = Empresa.builder()
                .nome("Other Corp")
                .email("other@corp.com")
                .senha(passwordEncoder.encode("senha123"))
                .cnpj("98.765.432/0001-11")
                .telefone("(84)88888-0000")
                .build();
        outraEmpresa = empresaRepository.save(outraEmpresa);

        empresaToken = jwtUtil.generateToken("tech@corp.com", "EMPRESA", "EMPRESA");
        outraEmpresaToken = jwtUtil.generateToken("other@corp.com", "EMPRESA", "EMPRESA");

        vaga = Vaga.builder()
                .titulo("Dev")
                .empresa("Tech Corp")
                .url("https://ex.com/1")
                .fonte(FonteVaga.EMPRESA)
                .empresaVinculada(empresa)
                .build();
        vaga = vagaRepository.save(vaga);
    }

    @Test
    void listarVagas_retornaApenasVagasDaEmpresaAutenticada() throws Exception {
        Vaga vagaOutra = Vaga.builder()
                .titulo("Designer")
                .empresa("Other Corp")
                .url("https://ex.com/2")
                .fonte(FonteVaga.EMPRESA)
                .empresaVinculada(outraEmpresa)
                .build();
        vagaRepository.save(vagaOutra);

        mockMvc.perform(get("/api/empresa/jobs")
                        .header("Authorization", "Bearer " + empresaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Dev"));
    }

    @Test
    void criarVaga_retorna200ComFonteEmpresa() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Backend Java",
                "empresa", "Tech Corp",
                "url", "https://ex.com/new"));

        mockMvc.perform(post("/api/empresa/jobs")
                        .header("Authorization", "Bearer " + empresaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Backend Java"))
                .andExpect(jsonPath("$.fonte").value("EMPRESA"))
                .andExpect(jsonPath("$.empresaNome").value("Tech Corp"));
    }

    @Test
    void atualizarVaga_propria_retorna200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Dev Senior",
                "empresa", "Tech Corp",
                "url", "https://ex.com/1"));

        mockMvc.perform(put("/api/empresa/jobs/" + vaga.getId())
                        .header("Authorization", "Bearer " + empresaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Dev Senior"));
    }

    @Test
    void deletarVaga_propria_retorna200() throws Exception {
        mockMvc.perform(delete("/api/empresa/jobs/" + vaga.getId())
                        .header("Authorization", "Bearer " + empresaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vaga removida com sucesso"));
    }

    @Test
    void deletarVaga_deOutraEmpresa_retorna403() throws Exception {
        mockMvc.perform(delete("/api/empresa/jobs/" + vaga.getId())
                        .header("Authorization", "Bearer " + outraEmpresaToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void perfil_retornaDadosDaEmpresa() throws Exception {
        mockMvc.perform(get("/api/empresa/perfil")
                        .header("Authorization", "Bearer " + empresaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Tech Corp"))
                .andExpect(jsonPath("$.email").value("tech@corp.com"))
                .andExpect(jsonPath("$.cnpj").value("12.345.678/0001-99"));
    }

    @Test
    void atualizarPerfil_retorna200() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Tech Corp Atualizada",
                "telefone", "(84)77777-0000"));

        mockMvc.perform(put("/api/empresa/perfil")
                        .header("Authorization", "Bearer " + empresaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Tech Corp Atualizada"));
    }
}
