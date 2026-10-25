package com.example.demo.dto;

import com.example.demo.model.Harvest;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class HarvestDto {
    private HarvestDto() {}

    public record Request(Long plantingId, BigDecimal mass) {}

    public record Response(Long id, Long plantingId, LocalDate date, BigDecimal mass) {
        public static Response from(Harvest h) {
            return new Response(h.getId(), h.getPlanting().getId(), h.getDate(), h.getMass());
        }
    }
}
