package com.senai.conta_bancaria_spring.domain.exceptions;

public class TaxaInvalidaException extends RuntimeException {
    public TaxaInvalidaException(String message) {
        super(message);
    }
}
