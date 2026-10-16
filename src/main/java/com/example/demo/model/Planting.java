package com.example.demo.model;

/** Посадка: грядка, культура, количество растений, статус. */
public record Planting(Long id, Long gardenBedId, Long cropId, Integer plantCount, PlantingStatus status) {
}
