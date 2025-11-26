package com.senai.conta_bancaria_spring.interface_ui.controller;

import com.senai.conta_bancaria_spring.application.DTO.PagamentoDTO;
import com.senai.conta_bancaria_spring.application.DTO.PagamentoResponseDTO;
import com.senai.conta_bancaria_spring.application.service.PagamentoAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
@Tag(name = "Pagamento", description = "Gestão Financeira")
public class PagamentoController {
    private final PagamentoAppService service;

    @PostMapping
    @Operation(summary = "Efetuar Pagamento", description = "Realiza o débito se houver autenticação IoT válida recente.")
    public ResponseEntity<PagamentoResponseDTO> pagar(@RequestBody PagamentoDTO dto) {
        return ResponseEntity.ok(service.realizarPagamento(dto));
    }
}