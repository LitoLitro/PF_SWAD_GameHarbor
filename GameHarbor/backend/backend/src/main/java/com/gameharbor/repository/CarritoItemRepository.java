package com.gameharbor.repository;

import com.gameharbor.model.entity.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Integer> {

    @Query("select ci from CarritoItem ci join fetch ci.juego where ci.carrito.id = :carritoId order by ci.id")
    List<CarritoItem> findByCarritoId(@Param("carritoId") Integer carritoId);

    Optional<CarritoItem> findByCarritoIdAndJuegoId(Integer carritoId, Integer juegoId);

    @Modifying
    @Query("delete from CarritoItem ci where ci.carrito.id = :carritoId")
    void deleteByCarritoId(@Param("carritoId") Integer carritoId);
}
