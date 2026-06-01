package br.com.unp.conectatech.repository;

import br.com.unp.conectatech.model.Vaga;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DataJpaTest
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VagaRepositoryFiltrosPostgresTest {

    @Autowired
    private VagaRepository vagaRepository;

    @Test
    void naoQuebraQuandoTodosOsFiltrosSaoNulos() {
        assertThatCode(() -> vagaRepository.buscarComFiltros(null, null, null, null, null))
                .doesNotThrowAnyException();

        List<Vaga> resultado = vagaRepository.buscarComFiltros(null, null, null, null, null);
        assertThat(resultado).isNotNull();
    }

    @Test
    void aplicaFiltroDeBuscaSemQuebrarComDatasNulas() {
        assertThatCode(() -> vagaRepository.buscarComFiltros("estagio", null, null, null, null))
                .doesNotThrowAnyException();
    }
}
