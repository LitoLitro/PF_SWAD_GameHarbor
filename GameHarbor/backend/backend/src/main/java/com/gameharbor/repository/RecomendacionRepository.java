package com.gameharbor.repository;

import com.gameharbor.model.entity.Recomendacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecomendacionRepository extends JpaRepository<Recomendacion, Integer> {

    /** Recomendaciones de un usuario que aun no posee, de mayor a menor puntaje. */
    @Query("""
            select r from Recomendacion r join fetch r.juego j
            where r.usuario.id = :usuarioId
              and j.estado = 'DISPONIBLE'
              and not exists (select 1 from Biblioteca b where b.usuario.id = :usuarioId and b.juego.id = j.id)
            order by r.puntaje desc
            """)
    List<Recomendacion> topParaUsuario(@Param("usuarioId") Integer usuarioId, Pageable pageable);
}
