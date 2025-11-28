package com.senai.conta_bancaria_spring.application.service;

import com.senai.conta_bancaria_spring.application.DTO.TaxaDTO;
import com.senai.conta_bancaria_spring.domain.entity.Taxa;
import com.senai.conta_bancaria_spring.domain.repository.TaxaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxaService {

    private final TaxaRepository repository;

    // Apenas ADMIN (Gerentes) podem cadastrar taxas conforme o PDF
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Taxa cadastrarTaxa(TaxaDTO dto) {
        return repository.save(dto.toEntity());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public List<Taxa> listarTaxas() {
        return repository.findAll();
    }
}

