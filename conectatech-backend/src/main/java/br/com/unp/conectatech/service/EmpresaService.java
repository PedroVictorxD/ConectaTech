package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.EmpresaDTO;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.Empresa;
import br.com.unp.conectatech.repository.EmpresaRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public List<EmpresaDTO> listarTodas() {
        return empresaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EmpresaDTO buscarPorId(Long id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com id: " + id));
        return toDTO(empresa);
    }

    public EmpresaDTO buscarPorEmail(String email) {
        Empresa empresa = empresaRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        return toDTO(empresa);
    }

    public EmpresaDTO atualizar(Long id, EmpresaDTO dto) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com id: " + id));

        empresa.setNome(dto.getNome());
        empresa.setTelefone(dto.getTelefone());
        empresa.setEndereco(dto.getEndereco());
        empresa.setDescricao(dto.getDescricao());
        empresa.setAreaAtuacao(dto.getAreaAtuacao());

        return toDTO(empresaRepository.save(empresa));
    }

    public EmpresaDTO atualizarPorEmail(String email, EmpresaDTO dto) {
        Empresa empresa = empresaRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));

        empresa.setNome(dto.getNome());
        empresa.setTelefone(dto.getTelefone());
        empresa.setEndereco(dto.getEndereco());
        empresa.setDescricao(dto.getDescricao());
        empresa.setAreaAtuacao(dto.getAreaAtuacao());

        return toDTO(empresaRepository.save(empresa));
    }

    public void deletar(Long id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa não encontrada com id: " + id);
        }
        empresaRepository.deleteById(id);
    }

    public EmpresaDTO toDTO(Empresa empresa) {
        return EmpresaDTO.builder()
                .id(empresa.getId())
                .nome(empresa.getNome())
                .email(empresa.getEmail())
                .cnpj(empresa.getCnpj())
                .telefone(empresa.getTelefone())
                .endereco(empresa.getEndereco())
                .descricao(empresa.getDescricao())
                .areaAtuacao(empresa.getAreaAtuacao())
                .criadoEm(empresa.getCriadoEm() != null
                        ? empresa.getCriadoEm().format(FORMATTER)
                        : null)
                .build();
    }
}
