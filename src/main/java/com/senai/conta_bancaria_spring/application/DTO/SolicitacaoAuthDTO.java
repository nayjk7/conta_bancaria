package com.senai.conta_bancaria_spring.application.DTO;

import lombok.Builder;

@Builder
public record SolicitacaoAuthDTO(String clienteId, String mensagem) {
}
