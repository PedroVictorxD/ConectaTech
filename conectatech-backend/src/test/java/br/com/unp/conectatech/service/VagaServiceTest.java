package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.VagaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class VagaServiceTest {

    @Autowired
    private VagaService vagaService;

    @Autowired
    private VagaRepository vagaRepository;

    @BeforeEach
    void setUp() {
        vagaRepository.deleteAll();

        vagaRepository.save(Vaga.builder()
                .titulo("Estagiario Java")
                .empresa("Tech Corp")
                .descricao("Vaga para dev Java")
                .localizacao("Mossoro, RN")
                .url("https://example.com/1")
                .fonte(FonteVaga.ADMIN)
                .build());

        vagaRepository.save(Vaga.builder()
                .titulo("Estagiario React")
                .empresa("Web Solutions")
                .descricao("Vaga frontend React")
                .localizacao("Natal, RN")
                .url("https://example.com/2")
                .fonte(FonteVaga.EMPRESA)
                .build());

        vagaRepository.save(Vaga.builder()
                .titulo("Analista RH")
                .empresa("Empresa RH")
                .descricao("Vaga RH")
                .localizacao("Mossoro, RN")
                .url("https://example.com/3")
                .fonte(FonteVaga.EMPRESA)
                .build());
    }

    @Test
    void listarTodas_retornaTodasAsVagas() {
        List<VagaDTO> vagas = vagaService.listarTodas();
        assertEquals(3, vagas.size());
    }

    @Test
    void buscarPorId_existente_retornaVaga() {
        Long id = vagaRepository.findAll().get(0).getId();
        VagaDTO vaga = vagaService.buscarPorId(id);
        assertNotNull(vaga);
        assertEquals("Estagiario Java", vaga.getTitulo());
    }

    @Test
    void buscarPorId_inexistente_lancaException() {
        assertThrows(ResourceNotFoundException.class, () -> vagaService.buscarPorId(99999L));
    }

    @Test
    void buscarPorTitulo_encontraResultados() {
        List<VagaDTO> vagas = vagaService.buscarPorTitulo("estagiario");
        assertEquals(2, vagas.size());
    }

    @Test
    void buscarPorTitulo_semResultados_retornaListaVazia() {
        List<VagaDTO> vagas = vagaService.buscarPorTitulo("python");
        assertTrue(vagas.isEmpty());
    }

    @Test
    void listarPorFonte_filtraCorretamente() {
        List<VagaDTO> vagas = vagaService.listarPorFonte(FonteVaga.ADMIN);
        assertEquals(1, vagas.size());
        assertEquals("Estagiario Java", vagas.get(0).getTitulo());
    }

    @Test
    void buscarComFiltros_porBusca_filtraPorTitulo() {
        List<VagaDTO> vagas = vagaService.buscarComFiltros("Java", null, null, null, null);
        assertEquals(1, vagas.size());
        assertEquals("Estagiario Java", vagas.get(0).getTitulo());
    }

    @Test
    void buscarComFiltros_porLocalizacao_filtra() {
        List<VagaDTO> vagas = vagaService.buscarComFiltros(null, "Mossoro", null, null, null);
        assertEquals(2, vagas.size());
    }

    @Test
    void buscarComFiltros_porFonte_filtra() {
        List<VagaDTO> vagas = vagaService.buscarComFiltros(null, null, "EMPRESA", null, null);
        assertEquals(2, vagas.size());
    }

    @Test
    void buscarComFiltros_fonteInvalida_retornaTodas() {
        List<VagaDTO> vagas = vagaService.buscarComFiltros(null, null, "INVALIDA", null, null);
        assertEquals(3, vagas.size());
    }

    @Test
    void buscarComFiltros_combinado_filtra() {
        List<VagaDTO> vagas = vagaService.buscarComFiltros("Estagiario", "Mossoro", null, null, null);
        assertEquals(1, vagas.size());
        assertEquals("Estagiario Java", vagas.get(0).getTitulo());
    }

    @Test
    void criar_retornaVagaCriada() {
        VagaDTO dto = VagaDTO.builder()
                .titulo("Nova Vaga")
                .empresa("Nova Empresa")
                .descricao("Desc")
                .localizacao("Natal")
                .url("https://example.com/nova")
                .fonte("ADMIN")
                .build();

        VagaDTO criada = vagaService.criar(dto);

        assertNotNull(criada.getId());
        assertEquals("Nova Vaga", criada.getTitulo());
        assertEquals("ADMIN", criada.getFonte());
    }

    @Test
    void atualizar_alteraCampos() {
        Vaga existente = vagaRepository.findAll().get(0);
        VagaDTO dto = VagaDTO.builder()
                .titulo("Titulo Atualizado")
                .empresa("Empresa Atualizada")
                .url(existente.getUrl())
                .build();

        VagaDTO atualizada = vagaService.atualizar(existente.getId(), dto);

        assertEquals("Titulo Atualizado", atualizada.getTitulo());
        assertEquals("Empresa Atualizada", atualizada.getEmpresa());
    }

    @Test
    void atualizar_inexistente_lancaException() {
        VagaDTO dto = VagaDTO.builder().titulo("X").empresa("Y").build();
        assertThrows(ResourceNotFoundException.class, () -> vagaService.atualizar(99999L, dto));
    }

    @Test
    void deletar_removeVaga() {
        Long id = vagaRepository.findAll().get(0).getId();
        vagaService.deletar(id);
        assertEquals(2, vagaRepository.count());
    }

    @Test
    void deletar_inexistente_lancaException() {
        assertThrows(ResourceNotFoundException.class, () -> vagaService.deletar(99999L));
    }

    @Test
    void toDTO_converteCorretamente() {
        Vaga vaga = vagaRepository.findAll().get(0);
        VagaDTO dto = vagaService.toDTO(vaga);

        assertEquals(vaga.getId(), dto.getId());
        assertEquals(vaga.getTitulo(), dto.getTitulo());
        assertEquals(vaga.getEmpresa(), dto.getEmpresa());
        assertEquals(vaga.getFonte().name(), dto.getFonte());
        assertNotNull(dto.getDataPublicacao());
    }
}
