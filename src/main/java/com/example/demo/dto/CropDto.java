package com.example.demo.dto;

import com.example.demo.model.Crop;
import com.example.demo.model.Season;

public final class CropDto {
    private CropDto() {}

    public record Request(String name, Season season, Double minAreaPerPlant) {}

    public record Response(Long id, String name, Season season, Double minAreaPerPlant) {
        public static Response from(Crop c) {
            return new Response(c.id(), c.name(), c.season(), c.minAreaPerPlant());
        }
    }
}
