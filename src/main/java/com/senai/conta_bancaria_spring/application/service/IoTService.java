package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.application.DTO.ValidacaoAuthDTO;
import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import com.senai.conta_bancaria_spring.domain.entity.CodigoAutenticacao;
import com.senai.conta_bancaria_spring.domain.exceptions.AutenticacaoIoTExpiradaException;
import com.senai.conta_bancaria_spring.domain.exceptions.EntidadeNaoEncontradoException;
import com.senai.conta_bancaria_spring.domain.repository.ClienteRepository;
import com.senai.conta_bancaria_spring.domain.repository.CodigoAutenticacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IoTService {
    private final CodigoAutenticacaoRepository codigoRepository;
    private final ClienteRepository clienteRepository;

    public void validarCodigoBiometrico(String clienteId) {
        CodigoAutenticacao auth = codigoRepository.findTopByClienteIdAndValidadoFalseOrderByExpiraEmDesc(clienteId)
                .orElseThrow(AutenticacaoIoTExpiradaException::new);

        if (auth.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new AutenticacaoIoTExpiradaException();
        }

        auth.setValidado(true);
        codigoRepository.save(auth);
    }
    @Transactional
    public void processarCodigoRecebido(ValidacaoAuthDTO dto) {
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new EntidadeNaoEncontradoException("Cliente IoT"));

        CodigoAutenticacao codigo = CodigoAutenticacao.builder()
                .cliente(cliente)
                .codigo(dto.getCodigoGerado())
                .validado(false)
                .expiraEm(LocalDateTime.now().plusMinutes(2))
                .build();

        codigoRepository.save(codigo);
        System.out.println("Código IoT salvo para cliente: " + cliente.getNome());
    }
}

