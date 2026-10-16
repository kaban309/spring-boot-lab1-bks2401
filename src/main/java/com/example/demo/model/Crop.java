package com.example.demo.model;

/** Культура: название, сезон посадки, минимальная площадь на одно растение (м²). */
public record Crop(Long id, String name, Season season, Double minAreaPerPlant) {
}
