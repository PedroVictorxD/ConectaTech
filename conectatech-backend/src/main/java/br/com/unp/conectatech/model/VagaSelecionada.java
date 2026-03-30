package br.com.unp.conectatech.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vagas_selecionadas", uniqueConstraints = @UniqueConstraint(columnNames = { "usuario_id", "vaga_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VagaSelecionada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vaga_id", nullable = false)
    private Vaga vaga;

    @Column(updatable = false)
    private LocalDateTime dataSelecionada;

    @PrePersist
    protected void onCreate() {
        dataSelecionada = LocalDateTime.now();
    }
}
