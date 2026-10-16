package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Crop;
import com.example.demo.model.Planting;
import com.example.demo.repository.CropRepository;
import com.example.demo.repository.PlantingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CropService {

    private final CropRepository crops;
    private final PlantingRepository plantings;
    private final PlantingService plantingService;

    public CropService(CropRepository crops, PlantingRepository plantings, PlantingService plantingService) {
        this.crops = crops;
        this.plantings = plantings;
        this.plantingService = plantingService;
    }

    public List<Crop> findAll() {
        return crops.findAll();
    }

    public Crop findById(Long id) {
        return crops.findById(id).orElseThrow(() -> new NotFoundException("Культура " + id + " не найдена"));
    }

    public Crop create(Crop data) {
        Validation.text(data.name(), "name", 100);
        Validation.required(data.season(), "season");
        Validation.positive(data.minAreaPerPlant(), "minAreaPerPlant");
        return crops.save(new Crop(null, data.name().trim(), data.season(), data.minAreaPerPlant()), null);
    }

    public Crop update(Long id, Crop patch) {
        Crop current = findById(id);
        String name = current.name();
        var season = current.season();
        Double minArea = current.minAreaPerPlant();
        if (patch.name() != null) {
            Validation.text(patch.name(), "name", 100);
            name = patch.name().trim();
        }
        if (patch.season() != null) {
            season = patch.season();
        }
        if (patch.minAreaPerPlant() != null) {
            Validation.positive(patch.minAreaPerPlant(), "minAreaPerPlant");
            minArea = patch.minAreaPerPlant();
        }
        Crop updated = new Crop(id, name, season, minArea);
        // изменение нормы площади не должно нарушать уже существующие посадки
        crops.save(updated, id);
        try {
            for (Planting p : plantings.findByCropId(id)) {
                plantingService.checkFits(p.gardenBedId(), p.id());
            }
        } catch (BadRequestException e) {
            crops.save(current, id); // откат
            throw e;
        }
        return updated;
    }

    /** Удаляет культуру и все посадки с ней (с урожаями). */
    public void delete(Long id) {
        findById(id);
        for (Planting p : plantings.findByCropId(id)) {
            plantingService.delete(p.id());
        }
        crops.deleteById(id);
    }
}
