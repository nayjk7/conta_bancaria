package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.application.DTO.PagamentoDTO;
import com.senai.conta_bancaria_spring.application.DTO.PagamentoResponseDTO;
import com.senai.conta_bancaria_spring.domain.entity.Conta;
import com.senai.conta_bancaria_spring.domain.entity.Pagamento;
import com.senai.conta_bancaria_spring.domain.entity.Taxa;
import com.senai.conta_bancaria_spring.domain.enums.TipoTaxa;
import com.senai.conta_bancaria_spring.domain.exceptions.EntidadeNaoEncontradoException;
import com.senai.conta_bancaria_spring.domain.exceptions.PagamentoInvalidoException;
import com.senai.conta_bancaria_spring.domain.repository.ContaRepository;
import com.senai.conta_bancaria_spring.domain.repository.PagamentoRepository;
import com.senai.conta_bancaria_spring.domain.repository.TaxaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoAppService {

    private final PagamentoRepository pagamentoRepository;
    private final ContaRepository contaRepository;
    private final TaxaRepository taxaRepository;
    private final PagamentoDomainService domainService;
    private final CodigoService codigoService;

    @Transactional
    public PagamentoResponseDTO realizarPagamento(PagamentoDTO dto){
        Conta conta = contaRepository.findByNumeroAndAtivaTrue(dto.numeroConta())
                .orElseThrow(() -> new EntidadeNaoEncontradoException("Conta"));

        validarDonoDaConta(conta);

        if (dto.dataVencimento() != null && dto.dataVencimento().isBefore(LocalDate.now())) {
            throw new PagamentoInvalidoException("Boleto vencido.");
        }

        codigoService.consumirCodigoValidado(conta.getCliente());

        List<Taxa> taxas = taxaRepository.findAllByTipo(TipoTaxa.PAGAMENTO);
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

        return PagamentoResponseDTO.fromEntity(pagamentoRepository.save(pag));
    }

    private void validarDonoDaConta(Conta conta) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new AccessDeniedException("Usuário não autenticado.");
        if (!conta.getCliente().getEmail().equals(authentication.getName())) {
            throw new AccessDeniedException("Acesso negado: Esta conta não pertence ao usuário logado.");
        }
    }
}