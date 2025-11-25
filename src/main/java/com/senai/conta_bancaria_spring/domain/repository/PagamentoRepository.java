package com.senai.conta_bancaria_spring.domain.repository;

import com.senai.conta_bancaria_spring.domain.entity.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository  extends JpaRepository<Pagamento, String> {
}
