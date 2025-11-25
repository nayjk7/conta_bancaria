package com.senai.conta_bancaria_spring.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidacaoAuthDTO {
    private String clienteId;
    private String codigoGerado;

}
