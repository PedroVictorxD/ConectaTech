package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.model.VagaSelecionada;
import br.com.unp.conectatech.repository.UsuarioRepository;
import br.com.unp.conectatech.repository.VagaRepository;
import br.com.unp.conectatech.repository.VagaSelecionadaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VagaSelecionadaService {

    private final VagaSelecionadaRepository vagaSelecionadaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VagaRepository vagaRepository;
    private final VagaService vagaService;

    public VagaSelecionadaService(VagaSelecionadaRepository vagaSelecionadaRepository,
            UsuarioRepository usuarioRepository,
            VagaRepository vagaRepository,
            VagaService vagaService) {
        this.vagaSelecionadaRepository = vagaSelecionadaRepository;
        this.usuarioRepository = usuarioRepository;
        this.vagaRepository = vagaRepository;
        this.vagaService = vagaService;
    }

    public void selecionar(String email, Long vagaId) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada com id: " + vagaId));

        if (vagaSelecionadaRepository.existsByUsuarioIdAndVagaId(usuario.getId(), vagaId)) {
            throw new IllegalArgumentException("Vaga já selecionada");
        }

        VagaSelecionada selecionada = VagaSelecionada.builder()
                .usuario(usuario)
                .vaga(vaga)
                .build();

        vagaSelecionadaRepository.save(selecionada);
    }

    public List<VagaDTO> listarMinhasVagas(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return vagaSelecionadaRepository.findByUsuarioId(usuario.getId()).stream()
                .map(vs -> vagaService.toDTO(vs.getVaga()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void removerSelecionada(String email, Long vagaId) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!vagaSelecionadaRepository.existsByUsuarioIdAndVagaId(usuario.getId(), vagaId)) {
            throw new ResourceNotFoundException("Vaga selecionada não encontrada");
        }

        vagaSelecionadaRepository.deleteByUsuarioIdAndVagaId(usuario.getId(), vagaId);
    }
}
