package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.VagaRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VagaService {

    private final VagaRepository vagaRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public VagaService(VagaRepository vagaRepository) {
        this.vagaRepository = vagaRepository;
    }

    public List<VagaDTO> listarTodas() {
        return vagaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<VagaDTO> listarPorFonte(FonteVaga fonte) {
        return vagaRepository.findByFonte(fonte).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<VagaDTO> buscarPorTitulo(String titulo) {
        return vagaRepository.findByTituloContainingIgnoreCase(titulo).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public VagaDTO buscarPorId(Long id) {
        Vaga vaga = vagaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada com id: " + id));
        return toDTO(vaga);
    }

    public VagaDTO criar(VagaDTO dto) {
        Vaga vaga = Vaga.builder()
                .titulo(dto.getTitulo())
                .empresa(dto.getEmpresa())
                .descricao(dto.getDescricao())
                .localizacao(dto.getLocalizacao())
                .url(dto.getUrl())
                .fonte(FonteVaga.valueOf(dto.getFonte()))
                .build();
        return toDTO(vagaRepository.save(vaga));
    }

    public VagaDTO atualizar(Long id, VagaDTO dto) {
        Vaga vaga = vagaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada com id: " + id));

        vaga.setTitulo(dto.getTitulo());
        vaga.setEmpresa(dto.getEmpresa());
        vaga.setDescricao(dto.getDescricao());
        vaga.setLocalizacao(dto.getLocalizacao());
        vaga.setUrl(dto.getUrl());

        return toDTO(vagaRepository.save(vaga));
    }

    public void deletar(Long id) {
        if (!vagaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vaga não encontrada com id: " + id);
        }
        vagaRepository.deleteById(id);
    }

    public List<VagaDTO> buscarComFiltros(String titulo, String localizacao, String fonte) {
        FonteVaga fonteEnum = null;
        if (fonte != null && !fonte.isBlank()) {
            try {
                fonteEnum = FonteVaga.valueOf(fonte.toUpperCase());
            } catch (IllegalArgumentException e) {
                // fonte inválida, ignora filtro
            }
        }
        return vagaRepository.buscarComFiltros(
                titulo != null && titulo.isBlank() ? null : titulo,
                localizacao != null && localizacao.isBlank() ? null : localizacao,
                fonteEnum
        ).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public VagaDTO toDTO(Vaga vaga) {
        return VagaDTO.builder()
                .id(vaga.getId())
                .titulo(vaga.getTitulo())
                .empresa(vaga.getEmpresa())
                .descricao(vaga.getDescricao())
                .localizacao(vaga.getLocalizacao())
                .url(vaga.getUrl())
                .fonte(vaga.getFonte().name())
                .dataPublicacao(vaga.getDataPublicacao() != null
                        ? vaga.getDataPublicacao().format(FORMATTER)
                        : null)
                .build();
    }
}
