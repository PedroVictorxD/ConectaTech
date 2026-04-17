package br.com.unp.conectatech.service;

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
class RedatorServiceTest {

    @Autowired
    private RedatorService redatorService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Vaga vaga;

    @BeforeEach
    void setUp() {
        vagaRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuarioRepository.save(Usuario.builder()
                .nome("Redator")
                .email("redator@email.com")
                .senha(passwordEncoder.encode("redator123"))
                .role(Role.REDATOR)
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
    void listarVagas_retornaTodas() {
        List<VagaDTO> vagas = redatorService.listarVagas();
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

        VagaDTO criada = redatorService.criarVaga(dto);

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

        VagaDTO atualizada = redatorService.atualizarVaga(vaga.getId(), dto);

        assertEquals("Vaga Atualizada", atualizada.getTitulo());
        assertEquals("Empresa Atualizada", atualizada.getEmpresa());
    }

    @Test
    void deletarVaga_removeDoSistema() {
        redatorService.deletarVaga(vaga.getId());
        assertTrue(redatorService.listarVagas().isEmpty());
    }

    @Test
    void deletarVaga_inexistente_lancaException() {
        assertThrows(ResourceNotFoundException.class,
                () -> redatorService.deletarVaga(99999L));
    }
}
