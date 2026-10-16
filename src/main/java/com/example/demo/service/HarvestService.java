package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Harvest;
import com.example.demo.repository.HarvestRepository;
import com.example.demo.repository.PlantingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HarvestService {

    private final HarvestRepository harvests;
    private final PlantingRepository plantings;

    public HarvestService(HarvestRepository harvests, PlantingRepository plantings) {
        this.harvests = harvests;
        this.plantings = plantings;
    }

    public List<Harvest> findAll() {
        return harvests.findAll();
    }

    public Harvest findById(Long id) {
        return harvests.findById(id).orElseThrow(() -> new NotFoundException("Сбор урожая " + id + " не найден"));
    }

    public Harvest create(Harvest data) {
        Validation.required(data.plantingId(), "plantingId");
        Validation.positive(data.mass(), "mass");
        checkPlanting(data.plantingId());
        // дату всегда задаёт сервер
        return harvests.save(new Harvest(null, data.plantingId(), LocalDate.now(), data.mass()), null);
    }

    public Harvest update(Long id, Harvest patch) {
        Harvest current = findById(id);
        Long plantingId = current.plantingId();
        Double mass = current.mass();
        if (patch.plantingId() != null) {
            checkPlanting(patch.plantingId());
            plantingId = patch.plantingId();
        }
        if (patch.mass() != null) {
            Validation.positive(patch.mass(), "mass");
            mass = patch.mass();
        }
        return harvests.save(new Harvest(id, plantingId, current.date(), mass), id);
    }

    public void delete(Long id) {
        findById(id);
        harvests.deleteById(id);
    }

    private void checkPlanting(Long plantingId) {
        if (!plantings.existsById(plantingId)) {
            throw new BadRequestException("Посадка " + plantingId + " не существует");
        }
    }
}
