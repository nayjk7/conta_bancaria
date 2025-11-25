package com.senai.conta_bancaria_spring.domain.repository;

import com.senai.conta_bancaria_spring.domain.entity.Taxa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxaRepository extends JpaRepository<Taxa, String> {
}
