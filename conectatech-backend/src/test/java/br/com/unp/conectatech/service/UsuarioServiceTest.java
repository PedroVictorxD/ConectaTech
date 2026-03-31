package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.AtualizarUsuarioRequest;
import br.com.unp.conectatech.dto.UsuarioDTO;
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
class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        usuarioRepository.save(Usuario.builder()
                .nome("Maria Silva")
                .email("maria@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .curso("CC")
                .periodo("6")
                .role(Role.STUDENT)
                .build());
    }

    @Test
    void buscarPorEmail_existente_retornaDTO() {
        UsuarioDTO dto = usuarioService.buscarPorEmail("maria@email.com");

        assertEquals("Maria Silva", dto.getNome());
        assertEquals("maria@email.com", dto.getEmail());
        assertEquals("CC", dto.getCurso());
        assertEquals("6", dto.getPeriodo());
        assertEquals("STUDENT", dto.getRole());
        assertNotNull(dto.getCriadoEm());
    }

    @Test
    void buscarPorEmail_inexistente_lancaException() {
        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.buscarPorEmail("naoexiste@email.com"));
    }

    @Test
    void atualizar_alteraNomeCursoPeriodo() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest();
        request.setNome("Maria Atualizada");
        request.setCurso("SI");
        request.setPeriodo("8");

        UsuarioDTO atualizado = usuarioService.atualizar("maria@email.com", request);

        assertEquals("Maria Atualizada", atualizado.getNome());
        assertEquals("SI", atualizado.getCurso());
        assertEquals("8", atualizado.getPeriodo());
    }

    @Test
    void atualizar_naoAlteraEmailNemRole() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest();
        request.setNome("Maria Nova");

        UsuarioDTO atualizado = usuarioService.atualizar("maria@email.com", request);

        assertEquals("maria@email.com", atualizado.getEmail());
        assertEquals("STUDENT", atualizado.getRole());
    }

    @Test
    void atualizar_emailInexistente_lancaException() {
        AtualizarUsuarioRequest request = new AtualizarUsuarioRequest();
        request.setNome("Ninguem");

        assertThrows(ResourceNotFoundException.class,
                () -> usuarioService.atualizar("naoexiste@email.com", request));
    }

    @Test
    void toDTO_converteCorretamente() {
        Usuario usuario = usuarioRepository.findByEmail("maria@email.com").get();
        UsuarioDTO dto = usuarioService.toDTO(usuario);

        assertEquals(usuario.getId(), dto.getId());
        assertEquals(usuario.getNome(), dto.getNome());
        assertEquals(usuario.getEmail(), dto.getEmail());
        assertEquals(usuario.getRole().name(), dto.getRole());
    }
}
