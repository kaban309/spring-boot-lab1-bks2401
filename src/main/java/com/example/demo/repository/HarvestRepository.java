package com.example.demo.repository;

import com.example.demo.model.Harvest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HarvestRepository extends JpaRepository<Harvest, Long> {
    boolean existsByPlantingId(Long plantingId);

    @Query("select h from Harvest h where h.planting.gardenBed.owner.id = :ownerId")
    List<Harvest> findAllByOwner(@Param("ownerId") Long ownerId);

    @Query("select count(h) > 0 from Harvest h where h.id = :id and h.planting.gardenBed.owner.id = :ownerId")
    boolean isOwnedBy(@Param("id") Long id, @Param("ownerId") Long ownerId);
}
