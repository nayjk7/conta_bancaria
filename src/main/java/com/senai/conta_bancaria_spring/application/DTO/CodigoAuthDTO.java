package com.senai.conta_bancaria_spring.application.DTO;

import java.time.LocalDateTime;

public record CodigoAuthDTO(
        String codigo,
        LocalDateTime expiraEm,
        String clienteCpf
){}