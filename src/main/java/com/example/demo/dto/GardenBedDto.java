package com.example.demo.dto;

import com.example.demo.model.GardenBed;

public final class GardenBedDto {
    private GardenBedDto() {}

    public record Request(Double area, Long ownerId, Boolean available) {}

    public record Response(Long id, Double area, Long ownerId, Boolean available) {
        public static Response from(GardenBed b) {
            return new Response(b.id(), b.area(), b.ownerId(), b.available());
        }
    }
}
