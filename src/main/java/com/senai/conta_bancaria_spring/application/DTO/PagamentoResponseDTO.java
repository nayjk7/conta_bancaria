package com.senai.conta_bancaria_spring.application.DTO;

import com.senai.conta_bancaria_spring.domain.entity.Pagamento;
import com.senai.conta_bancaria_spring.domain.entity.Taxa;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PagamentoResponseDTO(
        String id,
        String numeroConta,
        String codigoBoleto,
        BigDecimal valorPago,
        LocalDateTime dataPagamento,
        String status,
        List<String> taxasAplicadas
) {

    public static PagamentoResponseDTO fromEntity(Pagamento pagamento){
        List<String> nomesTaxas = pagamento.getTaxas().stream()
                .map(Taxa::getDescricao)
                .toList();

        return new PagamentoResponseDTO(
                pagamento.getId(),
                pagamento.getConta().getNumero(),
                pagamento.getBoleto(),
                pagamento.getValorPago(),
                pagamento.getDataPagamento(),
                pagamento.getStatus(),
                nomesTaxas

        );
    }

}
