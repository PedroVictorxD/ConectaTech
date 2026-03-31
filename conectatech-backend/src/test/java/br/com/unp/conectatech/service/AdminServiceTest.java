package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.UsuarioDTO;
import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Role;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.UsuarioRepository;
import br.com.unp.conectatech.repository.VagaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario admin;
    private Usuario student;
    private Vaga vaga;

    @BeforeEach
    void setUp() {
        vagaRepository.deleteAll();
        usuarioRepository.deleteAll();

        admin = usuarioRepository.save(Usuario.builder()
                .nome("Admin")
                .email("admin@email.com")
                .senha(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build());

        student = usuarioRepository.save(Usuario.builder()
                .nome("Aluno")
                .email("aluno@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .curso("CC")
                .periodo("6")
                .role(Role.STUDENT)
                .build());

        vaga = vagaRepository.save(Vaga.builder()
                .titulo("Vaga Teste")
                .empresa("Empresa Teste")
                .descricao("Descricao")
                .localizacao("Mossoro")
                .url("https://example.com/1")
                .fonte(FonteVaga.ADMIN)
                .build());
    }

    @Test
    void listarUsuarios_retornaTodos() {
        List<UsuarioDTO> usuarios = adminService.listarUsuarios();
        assertEquals(2, usuarios.size());
    }

    @Test
    void buscarUsuarioPorId_existente_retornaDTO() {
        UsuarioDTO dto = adminService.buscarUsuarioPorId(admin.getId());
        assertEquals("Admin", dto.getNome());
        assertEquals("admin@email.com", dto.getEmail());
        assertEquals("ADMIN", dto.getRole());
    }

    @Test
    void buscarUsuarioPorId_inexistente_lancaException() {
        assertThrows(ResourceNotFoundException.class,
                () -> adminService.buscarUsuarioPorId(99999L));
    }

    @Test
    void atualizarUsuario_alteraNomeECurso() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNome("Aluno Atualizado");
        dto.setCurso("SI");
        dto.setPeriodo("8");

        UsuarioDTO atualizado = adminService.atualizarUsuario(student.getId(), dto);

        assertEquals("Aluno Atualizado", atualizado.getNome());
        assertEquals("SI", atualizado.getCurso());
        assertEquals("8", atualizado.getPeriodo());
    }

    @Test
    void atualizarUsuario_alteraRole() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNome("Aluno Promovido");
        dto.setRole("ADMIN");

        UsuarioDTO atualizado = adminService.atualizarUsuario(student.getId(), dto);

        assertEquals("ADMIN", atualizado.getRole());
    }

    @Test
    void atualizarUsuario_roleInvalida_lancaException() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNome("Aluno");
        dto.setRole("INVALIDA");

        assertThrows(IllegalArgumentException.class,
                () -> adminService.atualizarUsuario(student.getId(), dto));
    }

    @Test
    void atualizarUsuario_inexistente_lancaException() {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setNome("Ninguem");

        assertThrows(ResourceNotFoundException.class,
                () -> adminService.atualizarUsuario(99999L, dto));
    }

    @Test
    void listarVagas_retornaTodas() {
        List<VagaDTO> vagas = adminService.listarVagas();
        assertEquals(1, vagas.size());
        assertEquals("Vaga Teste", vagas.get(0).getTitulo());
    }

    @Test
    void criarVaga_setaFonteADMIN() {
        VagaDTO dto = VagaDTO.builder()
                .titulo("Nova Vaga")
                .empresa("Nova Empresa")
                .url("https://example.com/nova")
                .build();

        VagaDTO criada = adminService.criarVaga(dto);

        assertEquals("Nova Vaga", criada.getTitulo());
        assertEquals("ADMIN", criada.getFonte());
    }

    @Test
    void atualizarVaga_alteraCampos() {
        VagaDTO dto = VagaDTO.builder()
                .titulo("Vaga Atualizada")
                .empresa("Empresa Atualizada")
                .url("https://example.com/1")
                .build();

        VagaDTO atualizada = adminService.atualizarVaga(vaga.getId(), dto);

        assertEquals("Vaga Atualizada", atualizada.getTitulo());
        assertEquals("Empresa Atualizada", atualizada.getEmpresa());
    }

    @Test
    void deletarVaga_removeDoSistema() {
        adminService.deletarVaga(vaga.getId());
        assertTrue(adminService.listarVagas().isEmpty());
    }

    @Test
    void deletarVaga_inexistente_lancaException() {
        assertThrows(ResourceNotFoundException.class,
                () -> adminService.deletarVaga(99999L));
    }
}
