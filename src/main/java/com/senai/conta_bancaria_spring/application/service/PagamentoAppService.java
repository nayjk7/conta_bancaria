package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.application.DTO.PagamentoDTO;
import com.senai.conta_bancaria_spring.application.DTO.PagamentoResponseDTO;
import com.senai.conta_bancaria_spring.domain.entity.Conta;
import com.senai.conta_bancaria_spring.domain.entity.Pagamento;
import com.senai.conta_bancaria_spring.domain.entity.Taxa;
import com.senai.conta_bancaria_spring.domain.exceptions.EntidadeNaoEncontradoException;
import com.senai.conta_bancaria_spring.domain.repository.ContaRepository;
import com.senai.conta_bancaria_spring.domain.repository.PagamentoRepository;
import com.senai.conta_bancaria_spring.domain.repository.TaxaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoAppService {

    private final PagamentoRepository pagamentoRepository;
    private final ContaRepository contaRepository;
    private final TaxaRepository taxaRepository;
    private final PagamentoService domainService;
    private final IoTService ioTService;

    @Transactional
    public PagamentoResponseDTO realizarPagamento(PagamentoDTO dto){
        Conta conta = contaRepository.findByNumeroAndAtivaTrue(dto.numeroConta())
                .orElseThrow(() -> new EntidadeNaoEncontradoException("Conta"));

        ioTService.solicitarCodigo(conta.getCliente().getCpf());

        List<Taxa> taxas = taxaRepository.findAll();
        var valorTotal = domainService.calcularTotal(dto.valorBoleto(), taxas);

        conta.sacar(valorTotal);
        contaRepository.save(conta);

        Pagamento pag = Pagamento.builder()
                .conta(conta)
                .boleto(dto.codigoBoleto())
                .valorPago(valorTotal)
                .dataPagamento(LocalDateTime.now())
                .status("SUCESSO")
                .taxas(taxas)
                .build();

        pagamentoRepository.save(pag);
        return PagamentoResponseDTO.fromEntity(pag);
    }
}
