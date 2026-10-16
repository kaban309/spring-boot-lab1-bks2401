package com.example.demo.dto;

import com.example.demo.model.Planting;
import com.example.demo.model.PlantingStatus;

public final class PlantingDto {
    private PlantingDto() {}

    public record Request(Long gardenBedId, Long cropId, Integer plantCount, PlantingStatus status) {}

    public record Response(Long id, Long gardenBedId, Long cropId, Integer plantCount, PlantingStatus status) {
        public static Response from(Planting p) {
            return new Response(p.id(), p.gardenBedId(), p.cropId(), p.plantCount(), p.status());
        }
    }
}
