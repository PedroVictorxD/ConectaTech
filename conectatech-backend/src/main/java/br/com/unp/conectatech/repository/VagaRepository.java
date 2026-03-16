package br.com.unp.conectatech.repository;

import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VagaRepository extends JpaRepository<Vaga, Long> {

    List<Vaga> findByFonte(FonteVaga fonte);

    List<Vaga> findByTituloContainingIgnoreCase(String titulo);

    boolean existsByUrl(String url);
}
