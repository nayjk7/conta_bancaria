package com.senai.conta_bancaria_spring.application.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagamentoDTO(
        String numeroConta,
        String codigoBoleto,
        BigDecimal valorBoleto,
        LocalDate dataVencimento
) {
}
