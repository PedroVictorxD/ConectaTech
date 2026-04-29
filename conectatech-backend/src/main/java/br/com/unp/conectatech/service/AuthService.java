package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.*;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.Role;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.repository.UsuarioRepository;
import br.com.unp.conectatech.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${auth.reset-password.expiration-hours}")
    private long resetPasswordExpirationHours;

    @Value("${auth.email-confirmation.expiration-hours}")
    private long emailConfirmationExpirationHours;

    public AuthService(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou senha inválidos"));

        if (Boolean.FALSE.equals(usuario.getEmailVerificado())) {
            throw new IllegalArgumentException("Confirme seu email antes de entrar");
        }

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new IllegalArgumentException("Email ou senha inválidos");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRole().name());
        return new LoginResponse(token, usuario.getNome(), usuario.getEmail(), usuario.getRole().name());
    }

    @Transactional
    public String recuperarSenha(RecuperarSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com este email"));

        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacao(token);
        usuario.setTokenExpiracao(LocalDateTime.now().plusHours(resetPasswordExpirationHours));
        usuarioRepository.save(usuario);
        emailService.enviarRecuperacaoSenha(usuario.getEmail(), usuario.getNome(), token, "aluno");

        return token;
    }

    @Transactional
    public void alterarSenha(AlterarSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByTokenRecuperacao(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token de recuperação inválido"));

        if (usuario.getTokenExpiracao().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token de recuperação expirado");
        }

        usuario.setSenha(passwordEncoder.encode(request.getNovaSenha()));
        usuario.setTokenRecuperacao(null);
        usuario.setTokenExpiracao(null);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void confirmarEmail(String token) {
        Usuario usuario = usuarioRepository.findByTokenConfirmacaoEmail(token)
                .orElseThrow(() -> new IllegalArgumentException("Token de confirmação inválido"));

        if (usuario.getTokenConfirmacaoExpiracao() == null
                || usuario.getTokenConfirmacaoExpiracao().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token de confirmação expirado");
        }

        usuario.setEmailVerificado(Boolean.TRUE);
        usuario.setTokenConfirmacaoEmail(null);
        usuario.setTokenConfirmacaoExpiracao(null);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario registrar(String nome, String email, String senha, String curso, String periodo) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        String tokenConfirmacao = UUID.randomUUID().toString();
        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .curso(curso)
                .periodo(periodo)
                .role(Role.STUDENT)
                .emailVerificado(Boolean.FALSE)
                .tokenConfirmacaoEmail(tokenConfirmacao)
                .tokenConfirmacaoExpiracao(LocalDateTime.now().plusHours(emailConfirmationExpirationHours))
                .build();

        Usuario salvo = usuarioRepository.save(usuario);
        emailService.enviarConfirmacaoEmail(salvo.getEmail(), salvo.getNome(), tokenConfirmacao, "aluno");
        return salvo;
    }
}
