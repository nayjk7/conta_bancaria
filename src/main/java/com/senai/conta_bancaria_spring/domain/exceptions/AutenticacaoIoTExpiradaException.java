package com.senai.conta_bancaria_spring.domain.exceptions;

public class AutenticacaoIoTExpiradaException extends RuntimeException {
    public AutenticacaoIoTExpiradaException() {
        super("Autenticação biométrica IoT não encontrada ou expirada.");
    }

    public AutenticacaoIoTExpiradaException(String message) {
        super(message);
    }
}
