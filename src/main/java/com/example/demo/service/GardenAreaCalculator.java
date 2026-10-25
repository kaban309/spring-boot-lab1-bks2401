package com.example.demo.service;

import com.example.demo.exception.ConflictException;
import com.example.demo.model.Crop;
import com.example.demo.model.GardenBed;
import com.example.demo.model.PlantingStatus;
import com.example.demo.repository.PlantingRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Расчёт занятой/свободной площади грядки — общее правило для CRUD и бизнес-операций. */
@Component
public class GardenAreaCalculator {

    private static final Long NO_EXCLUDE = -1L;

    private final PlantingRepository plantings;

    public GardenAreaCalculator(PlantingRepository plantings) {
        this.plantings = plantings;
    }

    /** Занятая площадь (все посадки, кроме собранных). excludeId — посадка, которую не учитываем. */
    public BigDecimal occupied(Long bedId, Long excludeId) {
        BigDecimal sum = plantings.sumOccupiedArea(bedId, PlantingStatus.HARVESTED,
                excludeId == null ? NO_EXCLUDE : excludeId);
        return sum == null ? BigDecimal.ZERO : sum;
    }

    /** Бросает 409, если count растений культуры crop не помещается на грядке. */
    public void ensureFits(GardenBed bed, Crop crop, int count, Long excludePlantingId) {
        BigDecimal need = crop.getMinAreaPerPlant().multiply(BigDecimal.valueOf(count));
        BigDecimal free = bed.getArea().subtract(occupied(bed.getId(), excludePlantingId));
        if (need.compareTo(free) > 0) {
            throw new ConflictException("Недостаточно места на грядке: нужно " + plain(need)
                    + " м², свободно " + plain(free.max(BigDecimal.ZERO)) + " м²");
        }
    }

    private static String plain(BigDecimal v) {
        return v.stripTrailingZeros().toPlainString();
    }
}
