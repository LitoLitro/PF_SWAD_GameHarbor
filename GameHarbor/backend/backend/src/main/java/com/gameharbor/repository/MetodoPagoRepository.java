package com.gameharbor.repository;

import com.gameharbor.model.entity.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {

    List<MetodoPago> findByActivoTrue();
}
