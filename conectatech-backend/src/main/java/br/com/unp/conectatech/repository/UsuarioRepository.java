package br.com.unp.conectatech.repository;

import br.com.unp.conectatech.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Usuario> findByTokenConfirmacaoEmail(String tokenConfirmacaoEmail);

    Optional<Usuario> findByTokenRecuperacao(String tokenRecuperacao);
}
