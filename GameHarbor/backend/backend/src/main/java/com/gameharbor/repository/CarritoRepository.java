package com.gameharbor.repository;

import com.gameharbor.model.entity.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Integer> {

    Optional<Carrito> findByUsuarioId(Integer usuarioId);
}
