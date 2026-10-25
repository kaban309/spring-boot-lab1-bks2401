package com.example.demo.repository;

import com.example.demo.model.Harvest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HarvestRepository extends JpaRepository<Harvest, Long> {
    boolean existsByPlantingId(Long plantingId);
}
