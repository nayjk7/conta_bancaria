package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "codigo_autenticacao")
public class CodigoAutenticacao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String codigo;
    private LocalDateTime expiraEm;
    private boolean validado;
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

}
