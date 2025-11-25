package com.senai.conta_bancaria_spring.domain.exceptions;

public class ValidacaoException extends RuntimeException {
    public ValidacaoException(String message) {
        super(message);
    }
}
