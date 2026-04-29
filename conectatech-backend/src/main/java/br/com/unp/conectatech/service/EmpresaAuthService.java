package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.*;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.Empresa;
import br.com.unp.conectatech.repository.EmpresaRepository;
import br.com.unp.conectatech.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EmpresaAuthService {

    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmpresaService empresaService;
    private final EmailService emailService;

    @Value("${auth.reset-password.expiration-hours}")
    private long resetPasswordExpirationHours;

    @Value("${auth.email-confirmation.expiration-hours}")
    private long emailConfirmationExpirationHours;

    public EmpresaAuthService(EmpresaRepository empresaRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            EmpresaService empresaService,
            EmailService emailService) {
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.empresaService = empresaService;
        this.emailService = emailService;
    }

    @Transactional
    public EmpresaDTO registrar(EmpresaRegistroRequest request) {
        if (empresaRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        if (empresaRepository.existsByCnpj(request.getCnpj())) {
            throw new IllegalArgumentException("CNPJ já cadastrado");
        }

        String tokenConfirmacao = UUID.randomUUID().toString();
        Empresa empresa = Empresa.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .cnpj(request.getCnpj())
                .telefone(request.getTelefone())
                .endereco(request.getEndereco())
                .descricao(request.getDescricao())
                .areaAtuacao(request.getAreaAtuacao())
                .emailVerificado(Boolean.FALSE)
                .tokenConfirmacaoEmail(tokenConfirmacao)
                .tokenConfirmacaoExpiracao(LocalDateTime.now().plusHours(emailConfirmationExpirationHours))
                .build();

        Empresa salva = empresaRepository.save(empresa);
        emailService.enviarConfirmacaoEmail(salva.getEmail(), salva.getNome(), tokenConfirmacao, "empresa");
        return empresaService.toDTO(salva);
    }

    public EmpresaLoginResponse login(EmpresaLoginRequest request) {
        Empresa empresa = empresaRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou senha inválidos"));

        if (Boolean.FALSE.equals(empresa.getEmailVerificado())) {
            throw new IllegalArgumentException("Confirme seu email antes de entrar");
        }

        if (!passwordEncoder.matches(request.getSenha(), empresa.getSenha())) {
            throw new IllegalArgumentException("Email ou senha inválidos");
        }

        String token = jwtUtil.generateToken(empresa.getEmail(), "EMPRESA", "EMPRESA");
        return new EmpresaLoginResponse(token, empresa.getNome(), empresa.getEmail(), empresa.getCnpj());
    }

    @Transactional
    public String recuperarSenha(RecuperarSenhaRequest request) {
        Empresa empresa = empresaRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada com este email"));

        String token = UUID.randomUUID().toString();
        empresa.setTokenRecuperacao(token);
        empresa.setTokenExpiracao(LocalDateTime.now().plusHours(resetPasswordExpirationHours));
        empresaRepository.save(empresa);
        emailService.enviarRecuperacaoSenha(empresa.getEmail(), empresa.getNome(), token, "empresa");

        return token;
    }

    @Transactional
    public void alterarSenha(AlterarSenhaRequest request) {
        Empresa empresa = empresaRepository.findByTokenRecuperacao(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token de recuperação inválido"));

        if (empresa.getTokenExpiracao().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token de recuperação expirado");
        }

        empresa.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        empresa.setTokenRecuperacao(null);
        empresa.setTokenExpiracao(null);
        empresaRepository.save(empresa);
    }

    @Transactional
    public void confirmarEmail(String token) {
        Empresa empresa = empresaRepository.findByTokenConfirmacaoEmail(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de confirmação inválido"));

        if (empresa.getTokenConfirmacaoExpiracao() == null
                || empresa.getTokenConfirmacaoExpiracao().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token de confirmação expirado");
        }

        empresa.setEmailVerificado(Boolean.TRUE);
        empresa.setTokenConfirmacaoEmail(null);
        empresa.setTokenConfirmacaoExpiracao(null);
        empresaRepository.save(empresa);
    }
}
