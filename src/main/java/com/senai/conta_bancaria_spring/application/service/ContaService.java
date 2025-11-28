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
    private final IoTService ioTService;
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


        ioTService.solicitarCodigo(conta.getCliente().getCpf());

        CodigoAutenticacao codigo = codigoRepository.findTopByClienteAndValidadoFalseOrderByExpiraEmDesc(conta.getCliente())
                        .orElseThrow(() -> new AutenticacaoIoTExpiradaException("Código inválido"));

        if (codigo.getExpiraEm().isBefore(LocalDateTime.now()) || !codigo.isValidado()){
            throw new AutenticacaoIoTExpiradaException("Autenticação falhou ou o código expirou.");

        }

        validarDonoDaConta(conta); // <-- MUDANÇA: Chama o novo método (sem admin)

        conta.sacar(dto.valor());
        return ContaResumoDTO.fromEntity(repository.save(conta));
    }

    public ContaResumoDTO depositar(String numeroConta, ValorSaqueDepositoDTO dto) {
        var conta = buscaContaAtivaPorNumero(numeroConta);

        validarDonoDaConta(conta); // <-- MUDANÇA: Chama o novo método (sem admin)

        conta.depositar(dto.valor());
        return ContaResumoDTO.fromEntity(repository.save(conta));
    }

    public ContaResumoDTO transferir(String numeroConta, TransferenciaDTO dto) {
        var contaOrigem = buscaContaAtivaPorNumero(numeroConta);

        // VERIFICAÇÃO IMPORTANTE: Só pode transferir se for dono da conta de ORIGEM
        validarDonoDaConta(contaOrigem); // <-- MUDANÇA: Chama o novo método (sem admin)

        var contaDestino = buscaContaAtivaPorNumero(dto.contaDestino());

        ioTService.solicitarCodigo(contaOrigem.getCliente().getCpf());

        CodigoAutenticacao codigo = codigoRepository.findTopByClienteAndValidadoFalseOrderByExpiraEmDesc(contaOrigem.getCliente())
                .orElseThrow(() -> new AutenticacaoIoTExpiradaException("Código expirado"));

        if (codigo.getExpiraEm().isBefore(LocalDateTime.now()) || !codigo.isValidado()){
            throw new AutenticacaoIoTExpiradaException("Autenticação falhou ou o código expirou.");

        }

        contaOrigem.transferir(dto.valor(), contaDestino);

        repository.save(contaDestino);
        return ContaResumoDTO.fromEntity(repository.save(contaOrigem));
    }

    public ContaResumoDTO aplicarRendimento(String numeroDaConta) {
        var conta = buscaContaAtivaPorNumero(numeroDaConta);

        validarDonoDaConta(conta); // <-- MUDANÇA: Chama o novo método (sem admin)

        if (conta instanceof ContaPoupanca poupanca) {
            poupanca.aplicarRendimento();
            return ContaResumoDTO.fromEntity(repository.save(conta));
        }
        throw new RendimentoInvalidoException();
    }

    // MÉTODO ANTIGO (Permite Admin) - MANTENHA ELE
    // Ele ainda é usado por buscarContaPorNumero, atualizarConta e deletarConta
    private void validarDonoDaContaOuAdmin(Conta conta) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        String emailUsuarioLogado = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !conta.getCliente().getEmail().equals(emailUsuarioLogado)) {
            throw new AccessDeniedException("Acesso negado: Esta conta não pertence ao usuário logado.");
        }
    }

    // --- NOVO MÉTODO PRIVADO (Apenas Cliente) ---
    private void validarDonoDaConta(Conta conta) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        String emailUsuarioLogado = authentication.getName();

        // Se o email do dono da conta for DIFERENTE do email do usuário logado
        if (!conta.getCliente().getEmail().equals(emailUsuarioLogado)) {
            // Não há verificação "isAdmin" aqui
            throw new AccessDeniedException("Acesso negado: Esta conta não pertence ao usuário logado.");
        }
    }
}