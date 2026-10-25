package com.example.demo.dto;

import com.example.demo.model.Planting;
import com.example.demo.model.PlantingStatus;

import java.time.LocalDate;

public final class PlantingDto {
    private PlantingDto() {}

    public record Request(Long gardenBedId, Long cropId, Integer plantCount, PlantingStatus status) {}

    public record Response(Long id, Long gardenBedId, Long cropId, Integer plantCount,
                           PlantingStatus status, LocalDate plantedAt) {
        public static Response from(Planting p) {
            return new Response(p.getId(), p.getGardenBed().getId(), p.getCrop().getId(),
                    p.getPlantCount(), p.getStatus(), p.getPlantedAt());
        }
    }
}
