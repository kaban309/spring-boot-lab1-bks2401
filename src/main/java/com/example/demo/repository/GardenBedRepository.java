package com.example.demo.repository;

import com.example.demo.model.GardenBed;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GardenBedRepository extends JpaRepository<GardenBed, Long> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from GardenBed b where b.id = :id")
    Optional<GardenBed> findByIdForUpdate(@Param("id") Long id);

    @Query("select b from GardenBed b where b.owner.id = :ownerId")
    List<GardenBed> findAllByOwner(@Param("ownerId") Long ownerId);

    @Query("select count(b) > 0 from GardenBed b where b.id = :id and b.owner.id = :ownerId")
    boolean isOwnedBy(@Param("id") Long id, @Param("ownerId") Long ownerId);
}
