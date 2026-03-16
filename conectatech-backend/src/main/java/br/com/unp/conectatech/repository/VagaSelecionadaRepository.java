package br.com.unp.conectatech.repository;

import br.com.unp.conectatech.model.VagaSelecionada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VagaSelecionadaRepository extends JpaRepository<VagaSelecionada, Long> {

    List<VagaSelecionada> findByUsuarioId(Long usuarioId);

    Optional<VagaSelecionada> findByUsuarioIdAndVagaId(Long usuarioId, Long vagaId);

    boolean existsByUsuarioIdAndVagaId(Long usuarioId, Long vagaId);

    void deleteByUsuarioIdAndVagaId(Long usuarioId, Long vagaId);
}
