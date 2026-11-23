package com.gameharbor.repository;

import com.gameharbor.model.entity.SesionJuego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SesionJuegoRepository extends JpaRepository<SesionJuego, Integer> {

    long countByServidorIdAndEstado(Integer servidorId, String estado);

    Optional<SesionJuego> findByUsuarioIdAndJuegoIdAndEstado(Integer usuarioId, Integer juegoId, String estado);

    Optional<SesionJuego> findByIdAndUsuarioId(Integer id, Integer usuarioId);

    @Query("select s.servidor.id from SesionJuego s where s.id = :sesionId and s.usuario.id = :usuarioId")
    Optional<Integer> findServidorIdByIdAndUsuarioId(@Param("sesionId") Integer sesionId,
                                                     @Param("usuarioId") Integer usuarioId);

    @Query("""
            select s from SesionJuego s join fetch s.juego join fetch s.servidor
            where s.usuario.id = :usuarioId and s.estado = :estado
            order by s.fechaInicio desc
            """)
    List<SesionJuego> findByUsuarioAndEstado(@Param("usuarioId") Integer usuarioId, @Param("estado") String estado);
}
