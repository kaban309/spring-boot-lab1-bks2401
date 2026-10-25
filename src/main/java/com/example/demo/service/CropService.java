package com.example.demo.service;

import com.example.demo.dto.CropDto;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Crop;
import com.example.demo.model.GardenBed;
import com.example.demo.model.Planting;
import com.example.demo.repository.CropRepository;
import com.example.demo.repository.PlantingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CropService {

    private final CropRepository crops;
    private final PlantingRepository plantings;
    private final GardenAreaCalculator calculator;

    public CropService(CropRepository crops, PlantingRepository plantings, GardenAreaCalculator calculator) {
        this.crops = crops;
        this.plantings = plantings;
        this.calculator = calculator;
    }

    @Transactional(readOnly = true)
    public List<Crop> findAll() {
        return crops.findAll();
    }

    @Transactional(readOnly = true)
    public Crop findById(Long id) {
        return crops.findById(id).orElseThrow(() -> new NotFoundException("Культура " + id + " не найдена"));
    }

    @Transactional
    public Crop create(CropDto.Request r) {
        Validation.text(r.name(), "name", 100);
        Validation.required(r.season(), "season");
        Validation.decimal(r.minAreaPerPlant(), "minAreaPerPlant", 5, 3);
        String name = r.name().trim();
        if (crops.existsByName(name)) {
            throw new ConflictException("Культура с таким названием уже существует");
        }
        return crops.save(new Crop(name, r.season(), r.minAreaPerPlant()));
    }

    @Transactional
    public Crop update(Long id, CropDto.Request r) {
        Crop crop = findById(id);
        if (r.name() != null) {
            Validation.text(r.name(), "name", 100);
            String name = r.name().trim();
            if (!name.equals(crop.getName()) && crops.existsByName(name)) {
                throw new ConflictException("Культура с таким названием уже существует");
            }
            crop.setName(name);
        }
        if (r.season() != null) {
            crop.setSeason(r.season());
        }
        if (r.minAreaPerPlant() != null) {
            Validation.decimal(r.minAreaPerPlant(), "minAreaPerPlant", 5, 3);
            crop.setMinAreaPerPlant(r.minAreaPerPlant());
            // новая норма площади не должна ломать уже существующие посадки; при нарушении транзакция откатывается
            List<GardenBed> beds = plantings.findByCropId(id).stream().map(Planting::getGardenBed).distinct().toList();
            for (GardenBed bed : beds) {
                if (calculator.occupied(bed.getId(), null).compareTo(bed.getArea()) > 0) {
                    throw new ConflictException("С новой нормой площади посадки не помещаются на грядке " + bed.getId());
                }
            }
        }
        return crop;
    }

    /** Удаляет культуру вместе с посадками и сборами. */
    @Transactional
    public void delete(Long id) {
        crops.delete(findById(id));
    }
}
