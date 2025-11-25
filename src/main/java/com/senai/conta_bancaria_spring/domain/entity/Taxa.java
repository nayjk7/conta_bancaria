package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "taxas")
public class Taxa {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String descricao;
    @Column(precision = 19, scale = 4)
    private BigDecimal percentual;
    @Column(precision = 19, scale = 2)
    private BigDecimal valorFixo;
}
