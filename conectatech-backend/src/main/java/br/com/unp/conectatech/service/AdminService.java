package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.UsuarioDTO;
import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Role;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final VagaService vagaService;

    public AdminService(UsuarioRepository usuarioRepository,
            UsuarioService usuarioService,
            VagaService vagaService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.vagaService = vagaService;
    }

    // === Gerenciamento de Usuários ===

    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(usuarioService::toDTO)
                .collect(Collectors.toList());
    }

    public UsuarioDTO buscarUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));
        return usuarioService.toDTO(usuario);
    }

    public UsuarioDTO atualizarUsuario(Long id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));

        usuario.setNome(dto.getNome());
        usuario.setCurso(dto.getCurso());
        usuario.setPeriodo(dto.getPeriodo());

        if (dto.getRole() != null) {
            usuario.setRole(Role.valueOf(dto.getRole()));
        }

        return usuarioService.toDTO(usuarioRepository.save(usuario));
    }

    // === Gerenciamento de Vagas ===

    public List<VagaDTO> listarVagas() {
        return vagaService.listarTodas();
    }

    public VagaDTO criarVaga(VagaDTO dto) {
        dto.setFonte(FonteVaga.ADMIN.name());
        return vagaService.criar(dto);
    }

    public VagaDTO atualizarVaga(Long id, VagaDTO dto) {
        return vagaService.atualizar(id, dto);
    }

    public void deletarVaga(Long id) {
        vagaService.deletar(id);
    }
}
