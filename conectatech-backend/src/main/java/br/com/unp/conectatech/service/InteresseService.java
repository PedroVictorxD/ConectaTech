package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.InteresseDTO;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.Interesse;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.InteresseRepository;
import br.com.unp.conectatech.repository.UsuarioRepository;
import br.com.unp.conectatech.repository.VagaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InteresseService {

    private final InteresseRepository interesseRepository;
    private final UsuarioRepository usuarioRepository;
    private final VagaRepository vagaRepository;
    private final EmailService emailService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public InteresseService(InteresseRepository interesseRepository,
            UsuarioRepository usuarioRepository,
            VagaRepository vagaRepository,
            EmailService emailService) {
        this.interesseRepository = interesseRepository;
        this.usuarioRepository = usuarioRepository;
        this.vagaRepository = vagaRepository;
        this.emailService = emailService;
    }

    public void demonstrarInteresse(String email, Long vagaId) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada com id: " + vagaId));

        if (interesseRepository.existsByUsuarioIdAndVagaId(usuario.getId(), vagaId)) {
            throw new IllegalArgumentException("Interesse já demonstrado nesta vaga");
        }

        Interesse interesse = Interesse.builder()
                .usuario(usuario)
                .vaga(vaga)
                .build();

        interesseRepository.save(interesse);

        if (vaga.getEmpresaVinculada() != null) {
            emailService.enviarNotificacaoInteresse(
                    vaga.getEmpresaVinculada().getEmail(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getCurso(),
                    vaga.getTitulo());
        }
    }

    @Transactional
    public void removerInteresse(String email, Long vagaId) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!interesseRepository.existsByUsuarioIdAndVagaId(usuario.getId(), vagaId)) {
            throw new ResourceNotFoundException("Interesse não encontrado");
        }

        interesseRepository.deleteByUsuarioIdAndVagaId(usuario.getId(), vagaId);
    }

    public List<InteresseDTO> listarInteressadosPorVaga(Long vagaId) {
        return interesseRepository.findByVagaId(vagaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private InteresseDTO toDTO(Interesse interesse) {
        return InteresseDTO.builder()
                .vagaId(interesse.getVaga().getId())
                .alunoNome(interesse.getUsuario().getNome())
                .alunoEmail(interesse.getUsuario().getEmail())
                .alunoCurso(interesse.getUsuario().getCurso())
                .dataInteresse(interesse.getDataInteresse() != null
                        ? interesse.getDataInteresse().format(FORMATTER)
                        : null)
                .build();
    }
}
