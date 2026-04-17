package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.model.*;
import br.com.unp.conectatech.repository.*;
import br.com.unp.conectatech.security.JwtUtil;
import br.com.unp.conectatech.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InteresseEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InteresseRepository interesseRepository;

    @Autowired
    private VagaSelecionadaRepository vagaSelecionadaRepository;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    private String studentToken;
    private String adminToken;
    private String empresaToken;
    private Vaga vagaEmpresa;
    private Vaga vagaAdmin;

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
                .periodo("6")
                .role(Role.STUDENT)
                .build();
        usuarioRepository.save(student);

        studentToken = authService.login(
                new br.com.unp.conectatech.dto.LoginRequest() {{
                    setEmail("aluno@email.com");
                    setSenha("senha123");
                }}).getToken();

        Usuario admin = Usuario.builder()
                .nome("Admin")
                .email("admin@email.com")
                .senha(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();
        usuarioRepository.save(admin);

        adminToken = authService.login(
                new br.com.unp.conectatech.dto.LoginRequest() {{
                    setEmail("admin@email.com");
                    setSenha("admin123");
                }}).getToken();

        Empresa empresa = Empresa.builder()
                .nome("Tech LTDA")
                .email("empresa@email.com")
                .senha(passwordEncoder.encode("empresa123"))
                .cnpj("12345678000199")
                .telefone("84999999999")
                .build();
        empresa = empresaRepository.save(empresa);

        empresaToken = jwtUtil.generateToken("empresa@email.com", "EMPRESA", "EMPRESA");

        vagaEmpresa = Vaga.builder()
                .titulo("Estagio Dev Web")
                .empresa("Tech LTDA")
                .descricao("Vaga de estagio")
                .localizacao("Mossoro, RN")
                .url("https://example.com/vaga/1")
                .fonte(FonteVaga.EMPRESA)
                .empresaVinculada(empresa)
                .build();
        vagaEmpresa = vagaRepository.save(vagaEmpresa);

        vagaAdmin = Vaga.builder()
                .titulo("Estagio Admin")
                .empresa("Empresa Admin")
                .descricao("Vaga admin")
                .localizacao("Natal, RN")
                .url("https://example.com/vaga/2")
                .fonte(FonteVaga.ADMIN)
                .build();
        vagaAdmin = vagaRepository.save(vagaAdmin);
    }

    @Test
    void demonstrarInteresse_comStudent_retorna200() throws Exception {
        mockMvc.perform(post("/api/jobs/" + vagaAdmin.getId() + "/interesse")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Interesse demonstrado com sucesso"));
    }

    @Test
    void demonstrarInteresse_duplicado_retorna400() throws Exception {
        mockMvc.perform(post("/api/jobs/" + vagaAdmin.getId() + "/interesse")
                .header("Authorization", "Bearer " + studentToken));

        mockMvc.perform(post("/api/jobs/" + vagaAdmin.getId() + "/interesse")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removerInteresse_comStudent_retorna200() throws Exception {
        mockMvc.perform(post("/api/jobs/" + vagaAdmin.getId() + "/interesse")
                .header("Authorization", "Bearer " + studentToken));

        mockMvc.perform(delete("/api/jobs/" + vagaAdmin.getId() + "/interesse")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Interesse removido com sucesso"));
    }

    @Test
    void demonstrarInteresse_semAuth_retorna403() throws Exception {
        mockMvc.perform(post("/api/jobs/" + vagaAdmin.getId() + "/interesse"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarInteressados_comoAdmin_retorna200() throws Exception {
        mockMvc.perform(post("/api/jobs/" + vagaAdmin.getId() + "/interesse")
                .header("Authorization", "Bearer " + studentToken));

        mockMvc.perform(get("/api/admin/jobs/" + vagaAdmin.getId() + "/interessados")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].alunoNome").value("Aluno Teste"))
                .andExpect(jsonPath("$[0].alunoEmail").value("aluno@email.com"))
                .andExpect(jsonPath("$[0].alunoCurso").value("CC"));
    }

    @Test
    void listarInteressados_comoEmpresa_retorna200() throws Exception {
        mockMvc.perform(post("/api/jobs/" + vagaEmpresa.getId() + "/interesse")
                .header("Authorization", "Bearer " + studentToken));

        mockMvc.perform(get("/api/empresa/jobs/" + vagaEmpresa.getId() + "/interessados")
                        .header("Authorization", "Bearer " + empresaToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].alunoNome").value("Aluno Teste"))
                .andExpect(jsonPath("$[0].alunoEmail").value("aluno@email.com"));
    }
}
