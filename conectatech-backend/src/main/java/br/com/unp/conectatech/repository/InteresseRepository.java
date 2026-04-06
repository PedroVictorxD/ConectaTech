package br.com.unp.conectatech.repository;

import br.com.unp.conectatech.model.Interesse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InteresseRepository extends JpaRepository<Interesse, Long> {
    List<Interesse> findByVagaId(Long vagaId);
    boolean existsByUsuarioIdAndVagaId(Long usuarioId, Long vagaId);
    long countByVagaId(Long vagaId);
    void deleteByUsuarioIdAndVagaId(Long usuarioId, Long vagaId);
}
