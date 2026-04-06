package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.EmpresaDTO;
import br.com.unp.conectatech.dto.EmpresaRegistroRequest;
import br.com.unp.conectatech.dto.VagaDTO;
import br.com.unp.conectatech.model.FonteVaga;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedatorService {

    private final VagaService vagaService;
    private final EmpresaService empresaService;
    private final EmpresaAuthService empresaAuthService;

    public RedatorService(VagaService vagaService, EmpresaService empresaService, EmpresaAuthService empresaAuthService) {
        this.vagaService = vagaService;
        this.empresaService = empresaService;
        this.empresaAuthService = empresaAuthService;
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

    public List<EmpresaDTO> listarEmpresas() {
        return empresaService.listarTodas();
    }

    public EmpresaDTO buscarEmpresa(Long id) {
        return empresaService.buscarPorId(id);
    }

    public EmpresaDTO criarEmpresa(EmpresaRegistroRequest request) {
        return empresaAuthService.registrar(request);
    }

    public EmpresaDTO atualizarEmpresa(Long id, EmpresaDTO dto) {
        return empresaService.atualizar(id, dto);
    }

    public void deletarEmpresa(Long id) {
        empresaService.deletar(id);
    }
}
