package com.example.demo.repository;

import com.example.demo.model.Harvest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HarvestRepository extends InMemoryRepository<Harvest> {
    @Override
    protected Harvest withId(Harvest e, Long id) {
        return new Harvest(id, e.plantingId(), e.date(), e.mass());
    }

    public List<Harvest> findByPlantingId(Long plantingId) {
        return findBy(h -> plantingId.equals(h.plantingId()));
    }
}
