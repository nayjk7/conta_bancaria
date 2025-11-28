package com.senai.conta_bancaria_spring.application.DTO;

import com.senai.conta_bancaria_spring.domain.entity.Taxa;
import com.senai.conta_bancaria_spring.domain.enums.TipoTaxa;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TaxaDTO(
        @NotBlank
        @Schema(description = "Nome da taxa", example = "IOF")
        String descricao,

        @Schema(description = "Percentual da taxa", example = "0.01")
        BigDecimal percentual,

        @Schema(description = "Valor fixo da taxa", example = "2.50")
        BigDecimal valorFixo,

        @NotNull(message = "O tipo da taxa é obrigatório")
        @Schema(description = "Tipo de operação onde a taxa aplica", example = "PAGAMENTO")
        TipoTaxa tipo
) {
    public Taxa toEntity() {
        return Taxa.builder()
                .descricao(this.descricao)
                .percentual(this.percentual)
                .valorFixo(this.valorFixo)
                .tipo(this.tipo) // Mapeando o novo campo
                .build();
    }
}

