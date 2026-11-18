package com.gameharbor.repository;

import com.gameharbor.model.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenRepository extends JpaRepository<Orden, Integer> {

    List<Orden> findByUsuarioIdOrderByFechaDesc(Integer usuarioId);
}
