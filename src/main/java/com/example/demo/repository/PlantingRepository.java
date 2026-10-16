package com.example.demo.repository;

import com.example.demo.model.Planting;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PlantingRepository extends InMemoryRepository<Planting> {
    @Override
    protected Planting withId(Planting e, Long id) {
        return new Planting(id, e.gardenBedId(), e.cropId(), e.plantCount(), e.status());
    }

    public List<Planting> findByGardenBedId(Long bedId) {
        return findBy(p -> bedId.equals(p.gardenBedId()));
    }

    public List<Planting> findByCropId(Long cropId) {
        return findBy(p -> cropId.equals(p.cropId()));
    }
}
