package com.example.demo.dto;

import com.example.demo.model.GardenBed;

import java.math.BigDecimal;

public final class GardenBedDto {
    private GardenBedDto() {}

    public record Request(BigDecimal area, Long ownerId, Boolean available) {}

    public record Response(Long id, BigDecimal area, Long ownerId, boolean available) {
        public static Response from(GardenBed b) {
            return new Response(b.getId(), b.getArea(), b.getOwner().getId(), b.isAvailable());
        }
    }
}
