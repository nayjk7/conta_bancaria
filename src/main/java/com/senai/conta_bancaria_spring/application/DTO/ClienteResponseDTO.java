package com.senai.conta_bancaria_spring.application.DTO;

import com.senai.conta_bancaria_spring.domain.entity.Cliente;
import com.senai.conta_bancaria_spring.domain.entity.Conta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CPF;

import java.util.List;

public record ClienteResponseDTO(
        @NotBlank
        @Schema(description = "ID único do cliente", example = "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8")
        String id,
        @NotBlank
        @Schema(description = "Nome do cliente", example = "Nayara Soares")
        String nome,
        @CPF
        @Schema(description = "CPF do cliente", example = "123.456.789-00")
        String cpf,
        @NotBlank
        @Schema(description = "Email de login do cliente", example = "nayara.soares@email.com")
        String email,
        @NotBlank
        @Schema(description = "Senha criptografada (apenas para referência, não usar)")
        String senha,
        @Schema(description = "Lista de contas (resumidas) associadas ao cliente")
        List<ContaResumoDTO> contas
) {
    public static ClienteResponseDTO fromEntity(Cliente cliente) {
        List<ContaResumoDTO> contasDTO = cliente.getContas().stream()
                .map(ContaResumoDTO::fromEntity)
                .toList();

        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getEmail(),
                cliente.getSenha(),
                contasDTO
        );
    }
}