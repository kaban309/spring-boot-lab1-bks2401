package com.example.demo.repository;

import com.example.demo.model.Planting;
import com.example.demo.model.PlantingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PlantingRepository extends JpaRepository<Planting, Long> {

    List<Planting> findByGardenBedIdAndStatus(Long gardenBedId, PlantingStatus status);

    boolean existsByGardenBedIdAndStatus(Long gardenBedId, PlantingStatus status);

    List<Planting> findByCropId(Long cropId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Planting p where p.id = :id")
    Optional<Planting> findByIdForUpdate(@Param("id") Long id);


    @Query("""
            select sum(p.plantCount * c.minAreaPerPlant)
            from Planting p join p.crop c
            where p.gardenBed.id = :bedId
              and p.status <> :finished
              and p.id <> :excludeId
            """)
    BigDecimal sumOccupiedArea(@Param("bedId") Long bedId,
                               @Param("finished") PlantingStatus finished,
                               @Param("excludeId") Long excludeId);
}
