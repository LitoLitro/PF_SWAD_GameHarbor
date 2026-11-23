package com.gameharbor.repository;

import com.gameharbor.model.entity.Carrito;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Integer> {

    Optional<Carrito> findByUsuarioId(Integer usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Carrito c where c.usuario.id = :usuarioId")
    Optional<Carrito> findByUsuarioIdForUpdate(@Param("usuarioId") Integer usuarioId);
}
