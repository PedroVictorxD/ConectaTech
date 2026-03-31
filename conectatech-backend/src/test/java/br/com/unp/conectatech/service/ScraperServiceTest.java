package br.com.unp.conectatech.service;

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
class ScraperServiceTest {

    @Autowired
    private ScraperService scraperService;

    @Autowired
    private VagaRepository vagaRepository;

    @BeforeEach
    void setUp() {
        vagaRepository.deleteAll();
    }

    @Test
    void executarScraping_comUrlInvalida_retornaListaVazia() {
        List<Vaga> vagas = scraperService.executarScraping();
        assertTrue(vagas.isEmpty());
    }

    @Test
    void executarScraping_naoLancaException() {
        assertDoesNotThrow(() -> scraperService.executarScraping());
    }

    @Test
    void executarScrapingAgendado_comScraperDesabilitado_naoImportaNada() {
        scraperService.executarScrapingAgendado();
        assertEquals(0, vagaRepository.count());
    }
}
