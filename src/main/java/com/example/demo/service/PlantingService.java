package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Crop;
import com.example.demo.model.GardenBed;
import com.example.demo.model.Harvest;
import com.example.demo.model.Planting;
import com.example.demo.model.PlantingStatus;
import com.example.demo.repository.CropRepository;
import com.example.demo.repository.GardenBedRepository;
import com.example.demo.repository.HarvestRepository;
import com.example.demo.repository.PlantingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlantingService {

    private final PlantingRepository plantings;
    private final GardenBedRepository beds;
    private final CropRepository crops;
    private final HarvestRepository harvests;

    public PlantingService(PlantingRepository plantings, GardenBedRepository beds,
                           CropRepository crops, HarvestRepository harvests) {
        this.plantings = plantings;
        this.beds = beds;
        this.crops = crops;
        this.harvests = harvests;
    }

    public List<Planting> findAll() {
        return plantings.findAll();
    }

    public Planting findById(Long id) {
        return plantings.findById(id).orElseThrow(() -> new NotFoundException("Посадка " + id + " не найдена"));
    }

    public Planting create(Planting data) {
        Validation.required(data.gardenBedId(), "gardenBedId");
        Validation.required(data.cropId(), "cropId");
        Validation.positive(data.plantCount(), "plantCount");
        GardenBed bed = beds.findById(data.gardenBedId())
                .orElseThrow(() -> new BadRequestException("Грядка " + data.gardenBedId() + " не существует"));
        if (!crops.existsById(data.cropId())) {
            throw new BadRequestException("Культура " + data.cropId() + " не существует");
        }
        if (!Boolean.TRUE.equals(bed.available())) {
            throw new BadRequestException("Грядка недоступна для посадки");
        }
        PlantingStatus status = data.status() != null ? data.status() : PlantingStatus.PLANNED;
        Planting saved = plantings.save(
                new Planting(null, data.gardenBedId(), data.cropId(), data.plantCount(), status), null);
        try {
            checkFits(saved.gardenBedId(), saved.id());
        } catch (BadRequestException e) {
            plantings.deleteById(saved.id()); // откат
            throw e;
        }
        return saved;
    }

    public Planting update(Long id, Planting patch) {
        Planting current = findById(id);
        Long bedId = current.gardenBedId();
        Long cropId = current.cropId();
        Integer count = current.plantCount();
        PlantingStatus status = current.status();
        if (patch.gardenBedId() != null) {
            GardenBed bed = beds.findById(patch.gardenBedId())
                    .orElseThrow(() -> new BadRequestException("Грядка " + patch.gardenBedId() + " не существует"));
            if (!Boolean.TRUE.equals(bed.available())) {
                throw new BadRequestException("Грядка недоступна для посадки");
            }
            bedId = patch.gardenBedId();
        }
        if (patch.cropId() != null) {
            if (!crops.existsById(patch.cropId())) {
                throw new BadRequestException("Культура " + patch.cropId() + " не существует");
            }
            cropId = patch.cropId();
        }
        if (patch.plantCount() != null) {
            Validation.positive(patch.plantCount(), "plantCount");
            count = patch.plantCount();
        }
        if (patch.status() != null) {
            status = patch.status();
        }
        Planting updated = plantings.save(new Planting(id, bedId, cropId, count, status), id);
        try {
            checkFits(bedId, id);
        } catch (BadRequestException e) {
            plantings.save(current, id); // откат
            throw e;
        }
        return updated;
    }

    /** Удаляет посадку и её сборы урожая. */
    public void delete(Long id) {
        findById(id);
        for (Harvest h : harvests.findByPlantingId(id)) {
            harvests.deleteById(h.id());
        }
        plantings.deleteById(id);
    }

    /**
     * Площадь грядки, занятая активными посадками (все, кроме собранных).
     * excludeId — посадка, которую не учитываем (при пересчёте её самой).
     */
    double occupiedArea(Long bedId, Long excludeId) {
        double sum = 0;
        for (Planting p : plantings.findByGardenBedId(bedId)) {
            if (p.id().equals(excludeId) || p.status() == PlantingStatus.HARVESTED) {
                continue;
            }
            Crop crop = crops.findById(p.cropId()).orElse(null);
            if (crop != null) {
                sum += p.plantCount() * crop.minAreaPerPlant();
            }
        }
        return sum;
    }

    /** Проверяет, что все активные посадки помещаются на грядке. */
    void checkFits(Long bedId, Long plantingId) {
        GardenBed bed = beds.findById(bedId).orElseThrow(() -> new BadRequestException("Грядка не существует"));
        if (occupiedArea(bedId, null) > bed.area()) {
            throw new BadRequestException("На грядке недостаточно площади для такого количества растений");
        }
    }
}
