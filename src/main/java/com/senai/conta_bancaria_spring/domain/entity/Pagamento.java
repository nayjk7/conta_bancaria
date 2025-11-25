package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "pagamentos")
public class Pagamento {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @Column(nullable = false)
    private String boleto;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorPago;

    @Column(nullable = false)
    private LocalDateTime dataPagamento;

    @Column(nullable = false)
    private String status;

    @ManyToMany
    @JoinTable(
            name = "pagamento_taxa",
            joinColumns = @JoinColumn(name = "pagamento_id"),
            inverseJoinColumns = @JoinColumn(name = "taxa_id")
    )
    private List<Taxa> taxas;
}

