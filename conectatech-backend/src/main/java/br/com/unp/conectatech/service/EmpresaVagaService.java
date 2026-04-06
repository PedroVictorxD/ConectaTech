package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.Empresa;
import br.com.unp.conectatech.model.FonteVaga;
import br.com.unp.conectatech.model.Vaga;
import br.com.unp.conectatech.repository.EmpresaRepository;
import br.com.unp.conectatech.repository.VagaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpresaVagaService {

    private final VagaRepository vagaRepository;
    private final EmpresaRepository empresaRepository;
    private final VagaService vagaService;

    public EmpresaVagaService(VagaRepository vagaRepository,
            EmpresaRepository empresaRepository,
            VagaService vagaService) {
        this.vagaRepository = vagaRepository;
        this.empresaRepository = empresaRepository;
        this.vagaService = vagaService;
    }

    public List<VagaDTO> listarVagas(String email) {
        Empresa empresa = getEmpresa(email);
        return vagaRepository.findByEmpresaVinculadaId(empresa.getId()).stream()
                .map(vagaService::toDTO)
                .collect(Collectors.toList());
    }

    public VagaDTO criarVaga(String email, VagaDTO dto) {
        Empresa empresa = getEmpresa(email);

        Vaga vaga = Vaga.builder()
                .titulo(dto.getTitulo())
                .empresa(dto.getEmpresa())
                .descricao(dto.getDescricao())
                .localizacao(dto.getLocalizacao())
                .url(dto.getUrl())
                .fonte(FonteVaga.EMPRESA)
                .empresaVinculada(empresa)
                .build();

        return vagaService.toDTO(vagaRepository.save(vaga));
    }

    public VagaDTO atualizarVaga(String email, Long vagaId, VagaDTO dto) {
        Vaga vaga = getVagaDaEmpresa(email, vagaId);

        vaga.setTitulo(dto.getTitulo());
        vaga.setEmpresa(dto.getEmpresa());
        vaga.setDescricao(dto.getDescricao());
        vaga.setLocalizacao(dto.getLocalizacao());
        vaga.setUrl(dto.getUrl());

        return vagaService.toDTO(vagaRepository.save(vaga));
    }

    public void deletarVaga(String email, Long vagaId) {
        Vaga vaga = getVagaDaEmpresa(email, vagaId);
        vagaRepository.delete(vaga);
    }

    public void validarVagaDaEmpresa(String email, Long vagaId) {
        getVagaDaEmpresa(email, vagaId);
    }

    private Empresa getEmpresa(String email) {
        return empresaRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
    }

    private Vaga getVagaDaEmpresa(String email, Long vagaId) {
        Empresa empresa = getEmpresa(email);
        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada com id: " + vagaId));

        if (vaga.getEmpresaVinculada() == null || !vaga.getEmpresaVinculada().getId().equals(empresa.getId())) {
            throw new AccessDeniedException("Vaga não pertence a esta empresa");
        }
        return vaga;
    }
}
