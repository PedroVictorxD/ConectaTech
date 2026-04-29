package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.AlterarSenhaRequest;
import br.com.unp.conectatech.dto.EmpresaLoginRequest;
import br.com.unp.conectatech.dto.EmpresaLoginResponse;
import br.com.unp.conectatech.dto.EmpresaRegistroRequest;
import br.com.unp.conectatech.dto.RecuperarSenhaRequest;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.Empresa;
import br.com.unp.conectatech.repository.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
class EmpresaAuthServiceTest {

    @Autowired
    private EmpresaAuthService empresaAuthService;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        empresaRepository.deleteAll();
    }

    @Test
    void registrar_criaEmpresaComSenhaCriptografada() {
        EmpresaRegistroRequest request = new EmpresaRegistroRequest();
        request.setNome("Empresa Teste");
        request.setEmail("empresa@email.com");
        request.setSenha("senha123");
        request.setCnpj("12.345.678/0001-99");
        request.setTelefone("84999990000");

        var empresa = empresaAuthService.registrar(request);

        assertNotNull(empresa.getId());
        assertEquals("Empresa Teste", empresa.getNome());
        assertEquals("empresa@email.com", empresa.getEmail());
        assertEquals(Boolean.FALSE, empresa.getEmailVerificado());
        assertTrue(passwordEncoder.matches("senha123",
                empresaRepository.findByEmail("empresa@email.com").orElseThrow().getSenha()));
        verify(emailService).enviarConfirmacaoEmail(
                "empresa@email.com",
                "Empresa Teste",
                empresaRepository.findByEmail("empresa@email.com").orElseThrow().getTokenConfirmacaoEmail(),
                "empresa");
    }

    @Test
    void login_comCredenciaisValidas_retornaToken() {
        salvarEmpresa("empresa@email.com", "senha123", true);

        EmpresaLoginRequest request = new EmpresaLoginRequest();
        request.setEmail("empresa@email.com");
        request.setSenha("senha123");

        EmpresaLoginResponse response = empresaAuthService.login(request);

        assertNotNull(response.getToken());
        assertEquals("Empresa Teste", response.getNome());
        assertEquals("empresa@email.com", response.getEmail());
    }

    @Test
    void recuperarSenha_geraTokenEEnviaEmail() {
        salvarEmpresa("empresa@email.com", "senha123", true);

        RecuperarSenhaRequest request = new RecuperarSenhaRequest();
        request.setEmail("empresa@email.com");

        String token = empresaAuthService.recuperarSenha(request);

        Empresa empresa = empresaRepository.findByEmail("empresa@email.com").orElseThrow();
        assertEquals(token, empresa.getTokenRecuperacao());
        assertNotNull(empresa.getTokenExpiracao());
        verify(emailService).enviarRecuperacaoSenha("empresa@email.com", "Empresa Teste", token, "empresa");
    }

    @Test
    void recuperarSenha_emailInexistente_lancaException() {
        RecuperarSenhaRequest request = new RecuperarSenhaRequest();
        request.setEmail("naoexiste@email.com");

        assertThrows(ResourceNotFoundException.class, () -> empresaAuthService.recuperarSenha(request));
    }

    @Test
    void alterarSenha_comTokenValido_alteraSenha() {
        salvarEmpresa("empresa@email.com", "senha123", true);

        RecuperarSenhaRequest recuperar = new RecuperarSenhaRequest();
        recuperar.setEmail("empresa@email.com");
        String token = empresaAuthService.recuperarSenha(recuperar);

        AlterarSenhaRequest alterar = new AlterarSenhaRequest();
        alterar.setToken(token);
        alterar.setNovaSenha("novasenha456");

        empresaAuthService.alterarSenha(alterar);

        Empresa empresa = empresaRepository.findByEmail("empresa@email.com").orElseThrow();
        assertTrue(passwordEncoder.matches("novasenha456", empresa.getSenha()));
        assertNull(empresa.getTokenRecuperacao());
        assertNull(empresa.getTokenExpiracao());
    }

    @Test
    void alterarSenha_comTokenInvalido_lancaException() {
        AlterarSenhaRequest request = new AlterarSenhaRequest();
        request.setToken("token-invalido");
        request.setNovaSenha("novasenha456");

        assertThrows(IllegalArgumentException.class, () -> empresaAuthService.alterarSenha(request));
    }

    @Test
    void alterarSenha_comTokenExpirado_lancaException() {
        Empresa empresa = Empresa.builder()
                .nome("Empresa Teste")
                .email("empresa@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .cnpj("12.345.678/0001-99")
                .telefone("84999990000")
                .emailVerificado(Boolean.TRUE)
                .tokenRecuperacao("token-expirado")
                .tokenExpiracao(java.time.LocalDateTime.now().minusMinutes(1))
                .build();
        empresaRepository.save(empresa);

        AlterarSenhaRequest request = new AlterarSenhaRequest();
        request.setToken("token-expirado");
        request.setNovaSenha("novasenha456");

        assertThrows(IllegalArgumentException.class, () -> empresaAuthService.alterarSenha(request));
    }

    @Test
    void login_comEmailNaoConfirmado_lancaException() {
        salvarEmpresa("empresa@email.com", "senha123", false);

        EmpresaLoginRequest request = new EmpresaLoginRequest();
        request.setEmail("empresa@email.com");
        request.setSenha("senha123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> empresaAuthService.login(request));
        assertEquals("Confirme seu email antes de entrar", ex.getMessage());
    }

    @Test
    void confirmarEmail_comTokenValido_confirmaEmpresa() {
        EmpresaRegistroRequest request = new EmpresaRegistroRequest();
        request.setNome("Empresa Teste");
        request.setEmail("empresa@email.com");
        request.setSenha("senha123");
        request.setCnpj("12.345.678/0001-99");
        request.setTelefone("84999990000");
        empresaAuthService.registrar(request);

        Empresa empresa = empresaRepository.findByEmail("empresa@email.com").orElseThrow();

        empresaAuthService.confirmarEmail(empresa.getTokenConfirmacaoEmail());

        Empresa atualizada = empresaRepository.findByEmail("empresa@email.com").orElseThrow();
        assertEquals(Boolean.TRUE, atualizada.getEmailVerificado());
        assertNull(atualizada.getTokenConfirmacaoEmail());
        assertNull(atualizada.getTokenConfirmacaoExpiracao());
    }

    @Test
    void confirmarEmail_comTokenInvalido_lancaException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> empresaAuthService.confirmarEmail("token-invalido"));
        assertEquals("Token de confirmação inválido", ex.getMessage());
    }

    @Test
    void confirmarEmail_comTokenExpirado_lancaException() {
        Empresa empresa = salvarEmpresa("empresa@email.com", "senha123", false);
        empresa.setTokenConfirmacaoEmail("token-confirmacao");
        empresa.setTokenConfirmacaoExpiracao(java.time.LocalDateTime.now().minusMinutes(1));
        empresaRepository.save(empresa);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> empresaAuthService.confirmarEmail("token-confirmacao"));
        assertEquals("Token de confirmação expirado", ex.getMessage());
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
