package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.domain.entity.Taxa;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PagamentoDomainService {
    public BigDecimal calcularTotal(BigDecimal valor, List<Taxa> taxas) {
        BigDecimal total = valor;
        for (Taxa t : taxas) {
            if (t.getValorFixo() != null) total = total.add(t.getValorFixo());
            if (t.getPercentual() != null) total = total.add(valor.multiply(t.getPercentual()));
        }
        return total;
    }
}
