package com.senai.conta_bancaria_spring.application.DTO;

import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import com.senai.conta_bancaria_spring.domain.entity.Conta;
import com.senai.conta_bancaria_spring.domain.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

import java.util.ArrayList;
import java.util.List;

public record ClienteRegistroDTO(
        @NotBlank
        @Schema(description = "Nome completo do cliente", example = "Nayara Soares")
        String nome,
        @NotBlank
        @CPF
        @Schema(description = "CPF do cliente (formatado ou não)", example = "123.456.789-00")
        String cpf,
        @NotBlank
        @Schema(description = "Email do cliente (será usado para login)", example = "nayara.soares@email.com")
        String email,
        @NotBlank
        @Schema(description = "Senha de acesso do cliente", example = "senha123")
        String senha,

        @Schema(description = "Dados da primeira conta a ser criada para este cliente")
        ContaResumoDTO contaDTO
) {
    public Cliente toEntity() {
        return Cliente.builder()
                .ativo(true)
                .nome(this.nome)
                .cpf(this.cpf)
                .email(this.email)
                .senha(this.senha)
                .contas(new ArrayList<Conta>())
                .role(Role.CLIENTE)
                .build();
    }
}