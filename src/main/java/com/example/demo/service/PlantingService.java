package com.example.demo.service;

import com.example.demo.dto.PlantingDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Crop;
import com.example.demo.model.GardenBed;
import com.example.demo.model.Planting;
import com.example.demo.model.PlantingStatus;
import com.example.demo.repository.CropRepository;
import com.example.demo.repository.GardenBedRepository;
import com.example.demo.repository.PlantingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PlantingService {

    private final PlantingRepository plantings;
    private final GardenBedRepository beds;
    private final CropRepository crops;
    private final GardenAreaCalculator calculator;

    public PlantingService(PlantingRepository plantings, GardenBedRepository beds,
                           CropRepository crops, GardenAreaCalculator calculator) {
        this.plantings = plantings;
        this.beds = beds;
        this.crops = crops;
        this.calculator = calculator;
    }

    @Transactional(readOnly = true)
    public List<Planting> findAll() {
        return plantings.findAll();
    }

    @Transactional(readOnly = true)
    public Planting findById(Long id) {
        return plantings.findById(id).orElseThrow(() -> new NotFoundException("Посадка " + id + " не найдена"));
    }

    @Transactional
    public Planting create(PlantingDto.Request r) {
        Validation.required(r.gardenBedId(), "gardenBedId");
        Validation.required(r.cropId(), "cropId");
        Validation.positive(r.plantCount(), "plantCount");
        GardenBed bed = lockBed(r.gardenBedId());
        Crop crop = crop(r.cropId());
        requireAvailable(bed);
        calculator.ensureFits(bed, crop, r.plantCount(), null);
        PlantingStatus status = r.status() != null ? r.status() : PlantingStatus.PLANNED;
        return plantings.save(new Planting(bed, crop, r.plantCount(), status, LocalDate.now()));
    }

    @Transactional
    public Planting update(Long id, PlantingDto.Request r) {
        Planting p = findById(id);
        GardenBed bed = p.getGardenBed();
        Crop crop = p.getCrop();
        int count = p.getPlantCount();
        PlantingStatus status = p.getStatus();

        if (r.gardenBedId() != null && !r.gardenBedId().equals(bed.getId())) {
            bed = lockBed(r.gardenBedId());
            requireAvailable(bed);
        } else {
            bed = beds.findByIdForUpdate(bed.getId()).orElseThrow();
        }
        if (r.cropId() != null) {
            crop = crop(r.cropId());
        }
        if (r.plantCount() != null) {
            Validation.positive(r.plantCount(), "plantCount");
            count = r.plantCount();
        }
        if (r.status() != null) {
            status = r.status();
        }
        if (status != PlantingStatus.HARVESTED) {
            calculator.ensureFits(bed, crop, count, id);
        }
        p.setGardenBed(bed);
        p.setCrop(crop);
        p.setPlantCount(count);
        p.setStatus(status);
        return p;
    }

    /** Удаляет посадку вместе со сборами урожая. */
    @Transactional
    public void delete(Long id) {
        plantings.delete(findById(id));
    }

    private GardenBed lockBed(Long id) {
        return beds.findByIdForUpdate(id)
                .orElseThrow(() -> new BadRequestException("Грядка " + id + " не существует"));
    }

    private Crop crop(Long id) {
        return crops.findById(id).orElseThrow(() -> new BadRequestException("Культура " + id + " не существует"));
    }

    private void requireAvailable(GardenBed bed) {
        if (!bed.isAvailable()) {
            throw new ConflictException("Грядка недоступна для посадки");
        }
    }
}
