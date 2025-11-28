package com.senai.conta_bancaria_spring.interface_ui.exception;

import com.senai.conta_bancaria_spring.domain.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.senai.conta_bancaria_spring.interface_ui.exception.ProblemDetailUtils.buildProblem;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValoresNegativoException.class)
    public ProblemDetail handleValoresNegativo(ValoresNegativoException ex,
                                               HttpServletRequest request) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Valores negativos não são permitidos.",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ContaMesmoTipoException.class)
    public ProblemDetail handleContaMesmoTipo(ContaMesmoTipoException ex,
                                                    HttpServletRequest request) {
        return buildProblem(
                HttpStatus.CONFLICT,
                "Não é possível criar uma conta do mesmo tipo para o mesmo cliente.",
                ex.getMessage(),
                request.getRequestURI()
        );

    }

    @ExceptionHandler(EntidadeNaoEncontradoException.class)
    public ProblemDetail handleEntidadeNaoEncontrado(EntidadeNaoEncontradoException ex,
                                                     HttpServletRequest request) {
        return buildProblem(
                HttpStatus.NOT_FOUND,
                "Entidade não encontrada.",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(RendimentoInvalidoException.class)
    public ProblemDetail handleRendimentoInvalido(RendimentoInvalidoException ex,
                                                  HttpServletRequest request) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Rendimento inválido.",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(SaldoinsuficienteException.class)
    public ProblemDetail handleSaldoInsuficiente(SaldoinsuficienteException ex,
                                                 HttpServletRequest request) {
        return buildProblem(
                HttpStatus.CONFLICT,
                "Saldo insuficiente para a operação.",
                ex.getMessage(),
                request.getRequestURI()
        );

    }

    @ExceptionHandler(TipoDeContaInvalidoException.class)
    public ProblemDetail handleTipoDeContaInvalido(TipoDeContaInvalidoException ex,
                                                   HttpServletRequest request) {
        return buildProblem(
                HttpStatus.BAD_REQUEST,
                "Tipo de conta inválido.",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(TransferenciaParaMesmaContaException.class)
    public ProblemDetail handleTransferenciaParaMesmaConta(TransferenciaParaMesmaContaException ex,
                                                           HttpServletRequest request) {
        return buildProblem(
                HttpStatus.CONFLICT,
                "Não é possível transferir para a mesma conta.",
                ex.getMessage(),
                request.getRequestURI()
        );

    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail badRequest(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail problem = buildProblem(
                HttpStatus.BAD_REQUEST,
                "Erro de validação",
                "Um ou mais campos são inválidos",
                request.getRequestURI()
        );

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        problem.setProperty("errors", errors);
        return problem;
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Tipo de parâmetro inválido");
        problem.setDetail(String.format(
                "O parâmetro '%s' deve ser do tipo '%s'. Valor recebido: '%s'",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido",
                ex.getValue()
        ));
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
    @ExceptionHandler(ConversionFailedException.class)
    public ProblemDetail handleConversionFailed(ConversionFailedException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Falha de conversão de parâmetro");
        problem.setDetail("Um parâmetro não pôde ser convertido para o tipo esperado.");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("error", ex.getMessage());
        return problem;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Erro de validação nos parâmetros");
        problem.setDetail("Um ou mais parâmetros são inválidos");
        problem.setInstance(URI.create(request.getRequestURI()));

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String campo = violation.getPropertyPath().toString();
            String mensagem = violation.getMessage();
            errors.put(campo, mensagem);
        });
        problem.setProperty("errors", errors);
        return problem;
    }
    @ExceptionHandler(AutenticacaoIoTExpiradaException.class)
    public ProblemDetail handleIoTExpirada(AutenticacaoIoTExpiradaException ex, HttpServletRequest request) {
        return buildProblem(
                HttpStatus.FORBIDDEN,
                "Autenticação Biométrica Necessária",
                ex.getMessage(),
                request.getRequestURI()
        );
    }

}


