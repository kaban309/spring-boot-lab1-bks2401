package com.example.demo.repository;

import com.example.demo.model.GardenBed;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GardenBedRepository extends InMemoryRepository<GardenBed> {
    @Override
    protected GardenBed withId(GardenBed e, Long id) {
        return new GardenBed(id, e.area(), e.ownerId(), e.available());
    }

    public List<GardenBed> findByOwnerId(Long ownerId) {
        return findBy(b -> ownerId.equals(b.ownerId()));
    }
}
