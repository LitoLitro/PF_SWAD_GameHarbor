package com.gameharbor.repository;

import com.gameharbor.model.entity.ServidorCloud;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServidorCloudRepository extends JpaRepository<ServidorCloud, Integer> {

    List<ServidorCloud> findByEstado(String estado);
}
