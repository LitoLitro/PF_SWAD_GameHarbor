package com.gameharbor.repository;

import com.gameharbor.model.entity.Biblioteca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BibliotecaRepository extends JpaRepository<Biblioteca, Integer> {

    @Query("select b from Biblioteca b join fetch b.juego where b.usuario.id = :usuarioId order by b.fechaAdquisicion desc")
    List<Biblioteca> findByUsuario(@Param("usuarioId") Integer usuarioId);

    boolean existsByUsuarioIdAndJuegoId(Integer usuarioId, Integer juegoId);
}
