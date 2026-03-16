package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.AtualizarUsuarioRequest;
import br.com.unp.conectatech.dto.UsuarioDTO;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioDTO buscarPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return toDTO(usuario);
    }

    public UsuarioDTO atualizar(String email, AtualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        usuario.setNome(request.getNome());
        usuario.setCurso(request.getCurso());
        usuario.setPeriodo(request.getPeriodo());

        return toDTO(usuarioRepository.save(usuario));
    }

    public UsuarioDTO toDTO(Usuario usuario) {
        return UsuarioDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .curso(usuario.getCurso())
                .periodo(usuario.getPeriodo())
                .role(usuario.getRole().name())
                .criadoEm(usuario.getCriadoEm() != null
                        ? usuario.getCriadoEm().format(FORMATTER)
                        : null)
                .build();
    }
}
