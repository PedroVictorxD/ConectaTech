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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VagaPaginacaoControllerTest {

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

        // Cria 15 vagas para testar paginação
        for (int i = 1; i <= 15; i++) {
            vagaRepository.save(Vaga.builder()
                    .titulo("Vaga " + i)
                    .empresa("Empresa " + i)
                    .url("https://example.com/" + i)
                    .fonte(FonteVaga.ADMIN)
                    .build());
        }
    }

    @Test
    void listarVagas_semParametros_retornaPrimeiraPaginaComDez() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    void listarVagas_page1Size10_retornaRestante() throws Exception {
        mockMvc.perform(get("/api/jobs?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.page").value(1));
    }

    @Test
    void listarVagas_size5_retornaApenasGinco() throws Exception {
        mockMvc.perform(get("/api/jobs?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void listarVagas_comFiltroEPaginacao_funcionaJuntos() throws Exception {
        mockMvc.perform(get("/api/jobs?busca=Vaga&page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(15));
    }
}
