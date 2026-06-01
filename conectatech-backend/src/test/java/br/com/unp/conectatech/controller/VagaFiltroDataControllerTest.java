package br.com.unp.conectatech.controller;

import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.EmpresaRepository;
import br.com.unp.conectatech.repository.InteresseRepository;
import br.com.unp.conectatech.repository.VagaRepository;
import br.com.unp.conectatech.repository.VagaSelecionadaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VagaFiltroDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VagaRepository vagaRepository;

    @Autowired
    private InteresseRepository interesseRepository;

    @Autowired
    private VagaSelecionadaRepository vagaSelecionadaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @BeforeEach
    void setUp() {
        interesseRepository.deleteAll();
        vagaSelecionadaRepository.deleteAll();
        vagaRepository.deleteAll();
        empresaRepository.deleteAll();

        // Vaga antiga: 10 dias atrás
        Vaga vagaAntiga = Vaga.builder()
                .titulo("Vaga Antiga")
                .empresa("Empresa A")
                .url("https://example.com/antiga")
                .fonte(FonteVaga.ADMIN)
                .dataPublicacao(LocalDateTime.now().minusDays(10))
                .build();
        vagaRepository.save(vagaAntiga);

        // Vaga recente: ontem
        Vaga vagaRecente = Vaga.builder()
                .titulo("Vaga Recente")
                .empresa("Empresa B")
                .url("https://example.com/recente")
                .fonte(FonteVaga.ADMIN)
                .dataPublicacao(LocalDateTime.now().minusDays(1))
                .build();
        vagaRepository.save(vagaRecente);

        // Vaga de hoje
        Vaga vagaHoje = Vaga.builder()
                .titulo("Vaga Hoje")
                .empresa("Empresa C")
                .url("https://example.com/hoje")
                .fonte(FonteVaga.ADMIN)
                .dataPublicacao(LocalDateTime.now())
                .build();
        vagaRepository.save(vagaHoje);
    }

    @Test
    void filtroDataInicio_retornaApenasVagasApos() throws Exception {
        String dataInicio = LocalDateTime.now().minusDays(2).toLocalDate().toString(); // yyyy-MM-dd

        mockMvc.perform(get("/api/jobs").param("dataInicio", dataInicio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)); // recente + hoje
    }

    @Test
    void filtroDataFim_retornaApenasVagasAntes() throws Exception {
        String dataFim = LocalDateTime.now().minusDays(5).toLocalDate().toString();

        mockMvc.perform(get("/api/jobs").param("dataFim", dataFim))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)); // só a antiga
    }

    @Test
    void filtroIntervalo_retornaVagasNoPeriodo() throws Exception {
        String dataInicio = LocalDateTime.now().minusDays(2).toLocalDate().toString();
        String dataFim = LocalDateTime.now().toLocalDate().toString();

        mockMvc.perform(get("/api/jobs")
                .param("dataInicio", dataInicio)
                .param("dataFim", dataFim))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)); // recente + hoje
    }

    @Test
    void filtroData_semParametros_retornaTodas() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void filtroData_formatoInvalido_retorna400() throws Exception {
        mockMvc.perform(get("/api/jobs").param("dataInicio", "01/01/2026"))
                .andExpect(status().isBadRequest());
    }
}
