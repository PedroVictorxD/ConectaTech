package br.com.unp.conectatech.repository;

import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VagaRepository extends JpaRepository<Vaga, Long> {

    List<Vaga> findByFonte(FonteVaga fonte);

    List<Vaga> findByTituloContainingIgnoreCase(String titulo);

    boolean existsByUrl(String url);

    @Query("SELECT v FROM Vaga v WHERE " +
           "(COALESCE(:busca, '') = '' OR (LOWER(v.titulo) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           "OR LOWER(v.empresa) LIKE LOWER(CONCAT('%', :busca, '%')) " +
           "OR LOWER(v.descricao) LIKE LOWER(CONCAT('%', :busca, '%')))) AND " +
           "(COALESCE(:localizacao, '') = '' OR LOWER(v.localizacao) LIKE LOWER(CONCAT('%', :localizacao, '%'))) AND " +
           "(:fonte IS NULL OR v.fonte = :fonte)")
    List<Vaga> buscarComFiltros(
            @Param("busca") String busca,
            @Param("localizacao") String localizacao,
            @Param("fonte") FonteVaga fonte);
}
