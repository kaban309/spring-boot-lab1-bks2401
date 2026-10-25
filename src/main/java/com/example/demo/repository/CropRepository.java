package com.example.demo.repository;

import com.example.demo.model.Crop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CropRepository extends JpaRepository<Crop, Long> {
    boolean existsByName(String name);
}
