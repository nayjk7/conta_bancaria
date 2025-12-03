package com.senai.conta_bancaria_spring.application.service;


import com.senai.conta_bancaria_spring.application.DTO.ContaAtualizacaoDTO;
import com.senai.conta_bancaria_spring.application.DTO.ContaResumoDTO;
import com.senai.conta_bancaria_spring.application.DTO.TransferenciaDTO;
import com.senai.conta_bancaria_spring.application.DTO.ValorSaqueDepositoDTO;
import com.senai.conta_bancaria_spring.domain.entity.CodigoAutenticacao;
import com.senai.conta_bancaria_spring.domain.entity.Conta;
import com.senai.conta_bancaria_spring.domain.entity.ContaCorrente;
import com.senai.conta_bancaria_spring.domain.entity.ContaPoupanca;
import com.senai.conta_bancaria_spring.domain.exceptions.AutenticacaoIoTExpiradaException;
import com.senai.conta_bancaria_spring.domain.exceptions.EntidadeNaoEncontradoException;
import com.senai.conta_bancaria_spring.domain.exceptions.RendimentoInvalidoException;
import com.senai.conta_bancaria_spring.domain.repository.CodigoAutenticacaoRepository;
import com.senai.conta_bancaria_spring.domain.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContaService {
    private final ContaRepository repository;
    private final CodigoAutenticacaoRepository codigoRepository;

    @Transactional(readOnly = true)
    public List<ContaResumoDTO> listarTodasContas() {
        return repository.findAllByAtivaTrue().stream()
                .map(ContaResumoDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ContaResumoDTO buscarContaPorNumero(String numero) {
        var conta = repository.findByNumeroAndAtivaTrue(numero)
                .orElseThrow(() -> new EntidadeNaoEncontradoException("Conta"));

        validarDonoDaContaOuAdmin(conta);

        return ContaResumoDTO.fromEntity(conta);
    }

    public ContaResumoDTO atualizarConta(String numeroConta, ContaAtualizacaoDTO dto) {
        var conta = buscaContaAtivaPorNumero(numeroConta);
        validarDonoDaContaOuAdmin(conta);

        if(conta instanceof ContaPoupanca poupanca){
            poupanca.setRendimento(dto.rendimento());
        } else if (conta instanceof ContaCorrente corrente) {
            corrente.setLimite(dto.limite());
            corrente.setTaxa(dto.taxa());
        }
        conta.setSaldo(dto.saldo());
        return ContaResumoDTO.fromEntity(repository.save(conta));
    }
    public void deletarConta(String numeroDaConta) {
        var conta = buscaContaAtivaPorNumero(numeroDaConta);
        validarDonoDaContaOuAdmin(conta);
        conta.setAtiva(false);
        repository.save(conta);
    }
    private Conta buscaContaAtivaPorNumero(String numeroDaConta) {
        return repository.findByNumeroAndAtivaTrue(numeroDaConta).orElseThrow(
                () -> new EntidadeNaoEncontradoException("Conta")
        );
    }

    public ContaResumoDTO sacar(String numeroConta, ValorSaqueDepositoDTO dto) {
        var conta = buscaContaAtivaPorNumero(numeroConta);
        validarDonoDaConta(conta);

        consumirCodigoIoT(conta);

        conta.sacar(dto.valor());
        return ContaResumoDTO.fromEntity(repository.save(conta));
    }

    public ContaResumoDTO depositar(String numeroConta, ValorSaqueDepositoDTO dto) {
        var conta = buscaContaAtivaPorNumero(numeroConta);
        validarDonoDaConta(conta);

        conta.depositar(dto.valor());
        return ContaResumoDTO.fromEntity(repository.save(conta));
    }

    public ContaResumoDTO transferir(String numeroConta, TransferenciaDTO dto) {
        var contaOrigem = buscaContaAtivaPorNumero(numeroConta);

       //Só pode transferir se for dono da conta de ORIGEM
        validarDonoDaConta(contaOrigem);
        var contaDestino = buscaContaAtivaPorNumero(dto.contaDestino());

        consumirCodigoIoT(contaOrigem);

        contaOrigem.transferir(dto.valor(), contaDestino);

        repository.save(contaDestino);
        return ContaResumoDTO.fromEntity(repository.save(contaOrigem));
    }

    public ContaResumoDTO aplicarRendimento(String numeroDaConta) {
        var conta = buscaContaAtivaPorNumero(numeroDaConta);
        validarDonoDaConta(conta);

        if (conta instanceof ContaPoupanca poupanca) {
            poupanca.aplicarRendimento();
            return ContaResumoDTO.fromEntity(repository.save(conta));
        }
        throw new RendimentoInvalidoException();
    }

    private void consumirCodigoIoT(Conta conta) {
        // Busca o último código que JÁ FOI VALIDADO pelo dispositivo (ValidadoTrue)
        CodigoAutenticacao codigo = codigoRepository.findTopByClienteAndValidadoTrueOrderByExpiraEmDesc(conta.getCliente())
                .orElseThrow(() -> new AutenticacaoIoTExpiradaException("Nenhuma autenticação biométrica válida encontrada. Solicite via App."));

        if (codigo.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new AutenticacaoIoTExpiradaException("O código biométrico expirou.");
        }

        // Invalida o código para não ser reutilizado (One-time use)
        codigo.setValidado(false);
        codigoRepository.save(codigo);
    }
    private Conta buscarContaAtivaPorNumero(String numeroDaConta) {
        return repository.findByNumeroAndAtivaTrue(numeroDaConta).orElseThrow(
                () -> new EntidadeNaoEncontradoException("Conta")
        );
    }


    private void validarDonoDaContaOuAdmin(Conta conta) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new AccessDeniedException("Usuário não autenticado.");
        String emailUsuarioLogado = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !conta.getCliente().getEmail().equals(emailUsuarioLogado)) {
            throw new AccessDeniedException("Acesso negado: Esta conta não pertence ao usuário logado.");
        }
    }

    private void validarDonoDaConta(Conta conta) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new AccessDeniedException("Usuário não autenticado.");
        if (!conta.getCliente().getEmail().equals(authentication.getName())) {
            throw new AccessDeniedException("Acesso negado: Esta conta não pertence ao usuário logado.");
        }
    }
}