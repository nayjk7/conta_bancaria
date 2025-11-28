package com.senai.conta_bancaria_spring.domain.exceptions;

public class PagamentoInvalidoException extends RuntimeException {
    public PagamentoInvalidoException(String message) {
        super(message);
    }
}
