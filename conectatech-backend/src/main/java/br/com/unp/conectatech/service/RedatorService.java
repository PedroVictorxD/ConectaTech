package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.model.FonteVaga;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedatorService {

    private final VagaService vagaService;

    public RedatorService(VagaService vagaService) {
        this.vagaService = vagaService;
    }

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
