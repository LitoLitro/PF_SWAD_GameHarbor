package com.gameharbor.repository;

import com.gameharbor.model.entity.ServidorCloud;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServidorCloudRepository extends JpaRepository<ServidorCloud, Integer> {

    List<ServidorCloud> findByEstado(String estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ServidorCloud s where s.estado = :estado order by s.id")
    List<ServidorCloud> findByEstadoForUpdate(@Param("estado") String estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ServidorCloud s where s.id = :id")
    Optional<ServidorCloud> findByIdForUpdate(@Param("id") Integer id);
}
