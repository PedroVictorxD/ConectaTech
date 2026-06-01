package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Role;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.UsuarioRepository;
import br.com.unp.conectatech.repository.VagaRepository;
import br.com.unp.conectatech.repository.VagaSelecionadaRepository;
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
class VagaSelecionadaServiceTest {

    @Autowired
    private VagaSelecionadaService vagaSelecionadaService;

    @Autowired
    private VagaSelecionadaRepository vagaSelecionadaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Vaga vaga1;
    private Vaga vaga2;

    @BeforeEach
    void setUp() {
        vagaSelecionadaRepository.deleteAll();
        vagaRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuarioRepository.save(Usuario.builder()
                .nome("Aluno")
                .email("aluno@email.com")
                .senha(passwordEncoder.encode("senha123"))
                .role(Role.STUDENT)
                .build());

        vaga1 = vagaRepository.save(Vaga.builder()
                .titulo("Vaga 1")
                .empresa("Empresa 1")
                .url("https://example.com/1")
                .fonte(FonteVaga.ADMIN)
                .build());

        vaga2 = vagaRepository.save(Vaga.builder()
                .titulo("Vaga 2")
                .empresa("Empresa 2")
                .url("https://example.com/2")
                .fonte(FonteVaga.EMPRESA)
                .build());
    }

    @Test
    void selecionar_adicionaVaga() {
        vagaSelecionadaService.selecionar("aluno@email.com", vaga1.getId());

        List<VagaDTO> minhas = vagaSelecionadaService.listarMinhasVagas("aluno@email.com");
        assertEquals(1, minhas.size());
        assertEquals("Vaga 1", minhas.get(0).getTitulo());
    }

    @Test
    void selecionar_duasVagas_retornaDuas() {
        vagaSelecionadaService.selecionar("aluno@email.com", vaga1.getId());
        vagaSelecionadaService.selecionar("aluno@email.com", vaga2.getId());

        List<VagaDTO> minhas = vagaSelecionadaService.listarMinhasVagas("aluno@email.com");
        assertEquals(2, minhas.size());
    }

    @Test
    void selecionar_mesmaVagaDuasVezes_lancaException() {
        vagaSelecionadaService.selecionar("aluno@email.com", vaga1.getId());

        assertThrows(IllegalArgumentException.class,
                () -> vagaSelecionadaService.selecionar("aluno@email.com", vaga1.getId()));
    }

    @Test
    void selecionar_vagaInexistente_lancaException() {
        assertThrows(ResourceNotFoundException.class,
                () -> vagaSelecionadaService.selecionar("aluno@email.com", 99999L));
    }

    @Test
    void selecionar_usuarioInexistente_lancaException() {
        assertThrows(ResourceNotFoundException.class,
                () -> vagaSelecionadaService.selecionar("naoexiste@email.com", vaga1.getId()));
    }

    @Test
    void listarMinhasVagas_semSelecoes_retornaListaVazia() {
        List<VagaDTO> minhas = vagaSelecionadaService.listarMinhasVagas("aluno@email.com");
        assertTrue(minhas.isEmpty());
    }

    @Test
    void removerSelecionada_removeVaga() {
        vagaSelecionadaService.selecionar("aluno@email.com", vaga1.getId());
        vagaSelecionadaService.selecionar("aluno@email.com", vaga2.getId());

        vagaSelecionadaService.removerSelecionada("aluno@email.com", vaga1.getId());

        List<VagaDTO> minhas = vagaSelecionadaService.listarMinhasVagas("aluno@email.com");
        assertEquals(1, minhas.size());
        assertEquals("Vaga 2", minhas.get(0).getTitulo());
    }

    @Test
    void removerSelecionada_naoSelecionada_lancaException() {
        assertThrows(ResourceNotFoundException.class,
                () -> vagaSelecionadaService.removerSelecionada("aluno@email.com", vaga1.getId()));
    }

    @Test
    void removerSelecionada_usuarioInexistente_lancaException() {
        assertThrows(ResourceNotFoundException.class,
                () -> vagaSelecionadaService.removerSelecionada("naoexiste@email.com", vaga1.getId()));
    }
}
