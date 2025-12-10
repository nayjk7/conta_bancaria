package com.senai.conta_bancaria_spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "dispositivos_IoT")
public class DispositivoIoT {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(unique = true)
    private String codigoSerial;
    private boolean ativo;
    @OneToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
}