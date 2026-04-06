package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.*;
import br.com.unp.conectatech.model.Empresa;
import br.com.unp.conectatech.repository.EmpresaRepository;
import br.com.unp.conectatech.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmpresaAuthService {

    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmpresaService empresaService;

    public EmpresaAuthService(EmpresaRepository empresaRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            EmpresaService empresaService) {
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.empresaService = empresaService;
    }

    public EmpresaDTO registrar(EmpresaRegistroRequest request) {
        if (empresaRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        if (empresaRepository.existsByCnpj(request.getCnpj())) {
            throw new IllegalArgumentException("CNPJ já cadastrado");
        }

        Empresa empresa = Empresa.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .cnpj(request.getCnpj())
                .telefone(request.getTelefone())
                .endereco(request.getEndereco())
                .descricao(request.getDescricao())
                .areaAtuacao(request.getAreaAtuacao())
                .build();

        return empresaService.toDTO(empresaRepository.save(empresa));
    }

    public EmpresaLoginResponse login(EmpresaLoginRequest request) {
        Empresa empresa = empresaRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(request.getSenha(), empresa.getSenha())) {
            throw new IllegalArgumentException("Email ou senha inválidos");
        }

        String token = jwtUtil.generateToken(empresa.getEmail(), "EMPRESA", "EMPRESA");
        return new EmpresaLoginResponse(token, empresa.getNome(), empresa.getEmail(), empresa.getCnpj());
    }
}
