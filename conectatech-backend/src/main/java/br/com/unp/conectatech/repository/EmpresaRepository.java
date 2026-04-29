package br.com.unp.conectatech.repository;

import br.com.unp.conectatech.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByEmail(String email);
    Optional<Empresa> findByCnpj(String cnpj);
    Optional<Empresa> findByTokenConfirmacaoEmail(String tokenConfirmacaoEmail);
    Optional<Empresa> findByTokenRecuperacao(String tokenRecuperacao);
    boolean existsByEmail(String email);
    boolean existsByCnpj(String cnpj);
}
