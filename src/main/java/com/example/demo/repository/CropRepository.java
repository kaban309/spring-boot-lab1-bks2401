package com.example.demo.repository;

import com.example.demo.model.Crop;
import org.springframework.stereotype.Repository;

@Repository
public class CropRepository extends InMemoryRepository<Crop> {
    @Override
    protected Crop withId(Crop e, Long id) {
        return new Crop(id, e.name(), e.season(), e.minAreaPerPlant());
    }
}
