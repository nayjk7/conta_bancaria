package com.senai.conta_bancaria_spring.domain.repository;

import com.senai.conta_bancaria_spring.domain.entity.DispositivoIoT;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DispositivoIoTRepository extends JpaRepository<DispositivoIoT, String> {
    Optional<DispositivoIoT> findByClienteIdAndAtivoTrue(String clienteId);
}
