package com.gameharbor.repository;

import com.gameharbor.model.entity.Categoria;
import com.gameharbor.model.entity.Juego;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.ArrayList;
import java.util.List;

public interface JuegoRepository extends JpaRepository<Juego, Integer>, JpaSpecificationExecutor<Juego> {

    /**
     * Filtro del catalogo publico: solo juegos DISPONIBLES, con texto (titulo) y categoria opcionales.
     * Se arma con Specification para no depender de parametros nulos en JPQL (problematico en SQL Server).
     */
    static Specification<Juego> catalogo(String texto, Integer categoriaId) {
        return (root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();
            filtros.add(cb.equal(root.get("estado"), "DISPONIBLE"));

            if (texto != null && !texto.isBlank()) {
                filtros.add(cb.like(cb.lower(root.get("titulo")), "%" + texto.trim().toLowerCase() + "%"));
            }
            if (categoriaId != null) {
                Join<Juego, Categoria> categoria = root.join("categorias", JoinType.INNER);
                filtros.add(cb.equal(categoria.get("id"), categoriaId));
            }
            query.distinct(true);
            return cb.and(filtros.toArray(new Predicate[0]));
        };
    }
}
