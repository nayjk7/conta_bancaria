package com.senai.conta_bancaria_spring.application.service;

import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPayload;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPublisher;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttSubscriber;
import com.senai.conta_bancaria_spring.application.DTO.ValidacaoAuthDTO;
import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import com.senai.conta_bancaria_spring.domain.entity.CodigoAutenticacao;
import com.senai.conta_bancaria_spring.domain.exceptions.EntidadeNaoEncontradoException;
import com.senai.conta_bancaria_spring.domain.repository.ClienteRepository;
import com.senai.conta_bancaria_spring.domain.repository.CodigoAutenticacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IoTService {
    private final CodigoAutenticacaoRepository codigoRepository;
    private final ClienteRepository clienteRepository;

        @MqttPublisher("banco/autenticacao/{clienteCpf}")
        public void solicitarCodigo(String clienteCpf) {
            System.out.println("Autenticação enviada para o cliente de CPF: " + clienteCpf);
        }

    @MqttSubscriber("banco/validacao/{clienteCpf}")
    public void processarCodigoRecebido(@MqttPayload ValidacaoAuthDTO dto) {
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


