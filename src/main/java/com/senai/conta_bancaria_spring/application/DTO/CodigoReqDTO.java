package com.senai.conta_bancaria_spring.application.DTO;

import com.senai.conta_bancaria_spring.domain.entity.CodigoAutenticacao;

import java.time.LocalDateTime;

public record CodigoReqDTO(
        String id,
        String codigo,
        LocalDateTime expiraEm,
        boolean validado,
        String clienteCpf
){
    public static CodigoReqDTO fromEntity(CodigoAutenticacao entity){
        return new CodigoReqDTO(
                entity.getId(),
                entity.getCodigo(),
                entity.getExpiraEm(),
                entity.isValidado(),
                entity.getCliente().getCpf()
        );
    }
}
