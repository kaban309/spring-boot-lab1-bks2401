package com.example.demo.dto;

import com.example.demo.model.Harvest;

import java.time.LocalDate;

public final class HarvestDto {
    private HarvestDto() {}

    /** Дата в запросе отсутствует намеренно: текущее время задаёт сервер. */
    public record Request(Long plantingId, Double mass) {}

    public record Response(Long id, Long plantingId, LocalDate date, Double mass) {
        public static Response from(Harvest h) {
            return new Response(h.id(), h.plantingId(), h.date(), h.mass());
        }
    }
}
