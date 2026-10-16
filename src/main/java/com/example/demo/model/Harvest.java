package com.example.demo.model;

import java.time.LocalDate;

/** Сбор урожая: посадка, дата (ставится сервером), масса собранного (кг). */
public record Harvest(Long id, Long plantingId, LocalDate date, Double mass) {
}
