package com.gameharbor.repository;

import com.gameharbor.model.entity.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResenaRepository extends JpaRepository<Resena, Integer> {

    @Query("select r from Resena r join fetch r.usuario where r.juego.id = :juegoId order by r.fecha desc")
    List<Resena> findByJuego(@Param("juegoId") Integer juegoId);

    Optional<Resena> findByUsuarioIdAndJuegoId(Integer usuarioId, Integer juegoId);

    @Query("select avg(r.calificacion) from Resena r where r.juego.id = :juegoId")
    Double promedioPorJuego(@Param("juegoId") Integer juegoId);

    long countByJuegoId(Integer juegoId);
}
