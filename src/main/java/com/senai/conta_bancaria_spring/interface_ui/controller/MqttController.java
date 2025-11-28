package com.senai.conta_bancaria_spring.interface_ui.controller;

import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPayload;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttPublisher;
import com.rafaelcosta.spring_mqttx.domain.annotation.MqttSubscriber;
import com.senai.conta_bancaria_spring.application.DTO.SolicitacaoAuthDTO;
import com.senai.conta_bancaria_spring.application.DTO.ValidacaoAuthDTO;
import com.senai.conta_bancaria_spring.application.service.IoTService;
import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import com.senai.conta_bancaria_spring.domain.exceptions.EntidadeNaoEncontradoException;
import com.senai.conta_bancaria_spring.domain.repository.ClienteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iot")
@RequiredArgsConstructor
@Tag(name = "Autenticação IoT", description = "Integração com dispositivos biométricos via MQTT")
public class MqttController {

    private final IoTService ioTService;
    private final ClienteRepository clienteRepository;

    @PostMapping("/solicitar/{cpf}")
    @MqttPublisher("banco/autenticacao/{cpf}")
    @Operation(summary = "Solicitar autenticação biométrica", description = "Envia comando MQTT para o dispositivo do cliente solicitar a digital.")
    public SolicitacaoAuthDTO solicitarAutenticacao(@PathVariable String cpf) {

        Cliente cliente = clienteRepository.findByCpfAndAtivoTrue(cpf)
                .orElseThrow(() -> new EntidadeNaoEncontradoException("Cliente não encontrado"));

        return SolicitacaoAuthDTO.builder()
                .clienteId(cliente.getId())
                .mensagem("SOLICITAR_BIOMETRIA")
                .build();
    }
    @MqttSubscriber("banco/validacao/+")
    public void receberConfirmacaoBiometria(@MqttPayload ValidacaoAuthDTO dto) {
        System.out.println("Payload recebido via MQTT: " + dto);
        ioTService.processarCodigoRecebido(dto);
    }
}
