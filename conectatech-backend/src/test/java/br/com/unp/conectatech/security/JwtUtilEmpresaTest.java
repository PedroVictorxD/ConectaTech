package br.com.unp.conectatech.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilEmpresaTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
                "TestSecretKeyParaTestes2026ConectaTechMinimo256bits1234567890",
                86400000);
    }

    @Test
    void generateToken_comTipoEmpresa_extractTipoRetornaEmpresa() {
        String token = jwtUtil.generateToken("empresa@email.com", "EMPRESA", "EMPRESA");

        assertEquals("EMPRESA", jwtUtil.extractTipo(token));
    }

    @Test
    void generateToken_semTipo_extractTipoRetornaUsuario() {
        String token = jwtUtil.generateToken("aluno@email.com", "STUDENT");

        assertEquals("USUARIO", jwtUtil.extractTipo(token));
    }

    @Test
    void generateToken_comTipoEmpresa_temRoleEmpresa() {
        String token = jwtUtil.generateToken("empresa@email.com", "EMPRESA", "EMPRESA");

        assertEquals("EMPRESA", jwtUtil.extractRole(token));
    }
}
