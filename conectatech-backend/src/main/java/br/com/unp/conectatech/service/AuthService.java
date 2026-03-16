package br.com.unp.conectatech.service;

import br.com.unp.conectatech.dto.*;
import br.com.unp.conectatech.exception.ResourceNotFoundException;
import br.com.unp.conectatech.model.Role;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.repository.UsuarioRepository;
import br.com.unp.conectatech.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Email ou senha inválidos"));

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new IllegalArgumentException("Email ou senha inválidos");
        }

        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRole().name());
        return new LoginResponse(token, usuario.getNome(), usuario.getEmail(), usuario.getRole().name());
    }

    public String recuperarSenha(RecuperarSenhaRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com este email"));

        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacao(token);
        usuario.setTokenExpiracao(LocalDateTime.now().plusHours(1));
        usuarioRepository.save(usuario);

        // TODO: Integrar com serviço de email para enviar o token
        return token;
    }

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

    public Usuario registrar(String nome, String email, String senha, String curso, String periodo) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nome(nome)
                .email(email)
                .senha(passwordEncoder.encode(senha))
                .curso(curso)
                .periodo(periodo)
                .role(Role.STUDENT)
                .build();

        return usuarioRepository.save(usuario);
    }
}
