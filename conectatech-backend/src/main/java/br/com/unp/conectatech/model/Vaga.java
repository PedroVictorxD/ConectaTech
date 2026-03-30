package br.com.unp.conectatech.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vagas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String empresa;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private String localizacao;

    @Column(nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FonteVaga fonte;

    private LocalDateTime dataPublicacao;

    @Column(updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        if (dataPublicacao == null) {
            dataPublicacao = LocalDateTime.now();
        }
    }
}
