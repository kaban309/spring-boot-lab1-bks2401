package com.example.demo.service;

import com.example.demo.dto.HarvestDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Harvest;
import com.example.demo.model.Planting;
import com.example.demo.repository.HarvestRepository;
import com.example.demo.repository.PlantingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public List<Harvest> findAll() {
        return harvests.findAll();
    }

    @Transactional(readOnly = true)
    public Harvest findById(Long id) {
        return harvests.findById(id).orElseThrow(() -> new NotFoundException("Сбор урожая " + id + " не найден"));
    }

    @Transactional
    public Harvest create(HarvestDto.Request r) {
        Validation.required(r.plantingId(), "plantingId");
        Validation.decimal(r.mass(), "mass", 7, 3);
        // дату всегда задаёт сервер
        return harvests.save(new Harvest(planting(r.plantingId()), LocalDate.now(), r.mass()));
    }

    @Transactional
    public Harvest update(Long id, HarvestDto.Request r) {
        Harvest h = findById(id);
        if (r.plantingId() != null) {
            h.setPlanting(planting(r.plantingId()));
        }
        if (r.mass() != null) {
            Validation.decimal(r.mass(), "mass", 7, 3);
            h.setMass(r.mass());
        }
        return h;
    }

    @Transactional
    public void delete(Long id) {
        harvests.delete(findById(id));
    }

    private Planting planting(Long id) {
        return plantings.findById(id).orElseThrow(() -> new BadRequestException("Посадка " + id + " не существует"));
    }
}
