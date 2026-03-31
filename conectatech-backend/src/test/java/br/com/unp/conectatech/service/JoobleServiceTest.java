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
class JoobleServiceTest {

    @Autowired
    private JoobleService joobleService;

    @Autowired
    private VagaRepository vagaRepository;

    @BeforeEach
    void setUp() {
        vagaRepository.deleteAll();
    }

    @Test
    void buscarVagasJooble_comApiKeyTeste_retornaListaVazia() {
        List<Vaga> vagas = joobleService.buscarVagasJooble("estagio", "Mossoro");
        assertTrue(vagas.isEmpty());
    }

    @Test
    void buscarVagasJooble_naoLancaException() {
        assertDoesNotThrow(() -> joobleService.buscarVagasJooble("java", "Natal"));
    }

    @Test
    void buscarVagasJooble_comKeywordsVazias_naoLancaException() {
        assertDoesNotThrow(() -> joobleService.buscarVagasJooble("", ""));
    }
}
