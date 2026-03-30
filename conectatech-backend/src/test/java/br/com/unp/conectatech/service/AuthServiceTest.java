package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.AlterarSenhaRequest;
import br.com.unp.conectatech.dto.LoginRequest;
import br.com.unp.conectatech.dto.LoginResponse;
import br.com.unp.conectatech.dto.RecuperarSenhaRequest;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.Role;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    @Test
    void registrar_criaUsuarioComRoleStudent() {
        Usuario usuario = authService.registrar("Joao", "joao@email.com", "senha123", "CC", "6");

        assertNotNull(usuario.getId());
        assertEquals("Joao", usuario.getNome());
        assertEquals("joao@email.com", usuario.getEmail());
        assertEquals(Role.STUDENT, usuario.getRole());
        assertTrue(passwordEncoder.matches("senha123", usuario.getSenha()));
    }

    @Test
    void registrar_comEmailDuplicado_lancaException() {
        authService.registrar("Joao", "joao@email.com", "senha123", "CC", "6");

        assertThrows(IllegalArgumentException.class,
                () -> authService.registrar("Maria", "joao@email.com", "outra123", "SI", "3"));
    }

    @Test
    void login_comCredenciaisValidas_retornaToken() {
        authService.registrar("Joao", "joao@email.com", "senha123", "CC", "6");

        LoginRequest request = new LoginRequest();
        request.setEmail("joao@email.com");
        request.setSenha("senha123");

        LoginResponse response = authService.login(request);

        assertNotNull(response.getToken());
        assertEquals("Joao", response.getNome());
        assertEquals("joao@email.com", response.getEmail());
        assertEquals("STUDENT", response.getRole());
    }

    @Test
    void login_comSenhaErrada_lancaException() {
        authService.registrar("Joao", "joao@email.com", "senha123", "CC", "6");

        LoginRequest request = new LoginRequest();
        request.setEmail("joao@email.com");
        request.setSenha("errada");

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void login_comEmailInexistente_lancaException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("naoexiste@email.com");
        request.setSenha("senha123");

        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
    }

    @Test
    void recuperarSenha_geraToken() {
        authService.registrar("Ana", "ana@email.com", "senha123", "CC", "6");

        RecuperarSenhaRequest request = new RecuperarSenhaRequest();
        request.setEmail("ana@email.com");

        String token = authService.recuperarSenha(request);

        assertNotNull(token);
        assertFalse(token.isBlank());

        Usuario usuario = usuarioRepository.findByEmail("ana@email.com").get();
        assertEquals(token, usuario.getTokenRecuperacao());
        assertNotNull(usuario.getTokenExpiracao());
    }

    @Test
    void recuperarSenha_emailInexistente_lancaException() {
        RecuperarSenhaRequest request = new RecuperarSenhaRequest();
        request.setEmail("naoexiste@email.com");

        assertThrows(ResourceNotFoundException.class, () -> authService.recuperarSenha(request));
    }

    @Test
    void alterarSenha_comTokenValido_alteraSenha() {
        authService.registrar("Ana", "ana@email.com", "senha123", "CC", "6");

        RecuperarSenhaRequest recuperar = new RecuperarSenhaRequest();
        recuperar.setEmail("ana@email.com");
        String token = authService.recuperarSenha(recuperar);

        AlterarSenhaRequest alterar = new AlterarSenhaRequest();
        alterar.setToken(token);
        alterar.setNovaSenha("novasenha456");

        authService.alterarSenha(alterar);

        Usuario usuario = usuarioRepository.findByEmail("ana@email.com").get();
        assertTrue(passwordEncoder.matches("novasenha456", usuario.getSenha()));
        assertNull(usuario.getTokenRecuperacao());
        assertNull(usuario.getTokenExpiracao());
    }

    @Test
    void alterarSenha_comTokenInvalido_lancaException() {
        AlterarSenhaRequest request = new AlterarSenhaRequest();
        request.setToken("token-invalido");
        request.setNovaSenha("novasenha456");

        assertThrows(IllegalArgumentException.class, () -> authService.alterarSenha(request));
    }
}
