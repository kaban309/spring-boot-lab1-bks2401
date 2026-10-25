package com.example.demo.dto;

import com.example.demo.model.Crop;
import com.example.demo.model.Season;

import java.math.BigDecimal;

public final class CropDto {
    private CropDto() {}

    public record Request(String name, Season season, BigDecimal minAreaPerPlant) {}

    public record Response(Long id, String name, Season season, BigDecimal minAreaPerPlant) {
        public static Response from(Crop c) {
            return new Response(c.getId(), c.getName(), c.getSeason(), c.getMinAreaPerPlant());
        }
    }
}
