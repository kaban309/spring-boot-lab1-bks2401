package com.example.demo.model;

/** Грядка: площадь (м²), владелец (User), доступность. */
public record GardenBed(Long id, Double area, Long ownerId, Boolean available) {
}
