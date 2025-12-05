package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import com.senai.conta_bancaria_spring.domain.entity.CodigoAutenticacao;
import com.senai.conta_bancaria_spring.domain.exceptions.AutenticacaoIoTExpiradaException;
import com.senai.conta_bancaria_spring.domain.repository.CodigoAutenticacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CodigoService {

    private final CodigoAutenticacaoRepository repository;

    @Transactional
    public void consumirCodigoValidado(Cliente cliente) {
        // Busca o último código que já foi validado pelo dispositivo (validado = true)
        CodigoAutenticacao codigo = repository.findTopByClienteAndValidadoTrueOrderByExpiraEmDesc(cliente)
                .orElseThrow(() -> new AutenticacaoIoTExpiradaException("Nenhuma autenticação biométrica válida encontrada. Realize a biometria no dispositivo."));

        // Verifica se o código expirou
        if (codigo.getExpiraEm().isBefore(LocalDateTime.now())) {
            throw new AutenticacaoIoTExpiradaException("O tempo da autenticação biométrica expirou.");
        }

        // "Consome" o código, invalidando-o para uso futuro (segurança para evitar replay attacks)
        codigo.setValidado(false);
        repository.save(codigo);
    }
}