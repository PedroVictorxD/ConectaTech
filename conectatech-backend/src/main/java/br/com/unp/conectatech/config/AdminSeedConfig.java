package br.com.unp.conectatech.config;

import br.com.unp.conectatech.model.Role;
import br.com.unp.conectatech.model.Usuario;
import br.com.unp.conectatech.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeedConfig {

    private static final Logger log = LoggerFactory.getLogger(AdminSeedConfig.class);

    @Bean
    CommandLineRunner seedAdmin(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = System.getenv("ADMIN_EMAIL") != null
                    ? System.getenv("ADMIN_EMAIL") : "admin@conectatech.com";
            String adminPassword = System.getenv("ADMIN_PASSWORD") != null
                    ? System.getenv("ADMIN_PASSWORD") : "admin123456";

            if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
                Usuario admin = Usuario.builder()
                        .nome("Administrador")
                        .email(adminEmail)
                        .senha(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .emailVerificado(Boolean.TRUE)
                        .build();
                usuarioRepository.save(admin);
                log.info(">>> Admin seed criado com email: {}", adminEmail);
            } else {
                log.info(">>> Admin ja existe: {}", adminEmail);
            }
        };
    }
}
