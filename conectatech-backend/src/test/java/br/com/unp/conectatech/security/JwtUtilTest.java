package br.com.unp.conectatech.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
                "TestSecretKeyParaTestes2026ConectaTechMinimo256bits1234567890",
                86400000);
    }

    @Test
    void generateToken_retornaTokenValido() {
        String token = jwtUtil.generateToken("aluno@email.com", "STUDENT");

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertTrue(jwtUtil.isTokenValid(token));
    }

    @Test
    void extractEmail_retornaEmailCorreto() {
        String token = jwtUtil.generateToken("aluno@email.com", "STUDENT");

        assertEquals("aluno@email.com", jwtUtil.extractEmail(token));
    }

    @Test
    void extractRole_retornaRoleCorreta() {
        String token = jwtUtil.generateToken("admin@email.com", "ADMIN");

        assertEquals("ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    void isTokenValid_comTokenInvalido_retornaFalse() {
        assertFalse(jwtUtil.isTokenValid("token.invalido.aqui"));
    }

    @Test
    void isTokenValid_comTokenVazio_retornaFalse() {
        assertFalse(jwtUtil.isTokenValid(""));
    }

    @Test
    void isTokenValid_comTokenNull_retornaFalse() {
        assertFalse(jwtUtil.isTokenValid(null));
    }

    @Test
    void tokenDeOutraChave_naoEhValido() {
        JwtUtil outraInstancia = new JwtUtil(
                "OutraChaveCompletamenteDiferente2026TestKey256bitsMinimo12345",
                86400000);
        String tokenOutraChave = outraInstancia.generateToken("aluno@email.com", "STUDENT");

        assertFalse(jwtUtil.isTokenValid(tokenOutraChave));
    }

    @Test
    void tokenExpirado_naoEhValido() {
        JwtUtil expiradoUtil = new JwtUtil(
                "TestSecretKeyParaTestes2026ConectaTechMinimo256bits1234567890",
                -1000);
        String tokenExpirado = expiradoUtil.generateToken("aluno@email.com", "STUDENT");

        assertFalse(jwtUtil.isTokenValid(tokenExpirado));
    }
}
