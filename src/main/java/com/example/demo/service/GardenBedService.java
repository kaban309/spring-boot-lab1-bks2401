package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.GardenBed;
import com.example.demo.model.Planting;
import com.example.demo.repository.GardenBedRepository;
import com.example.demo.repository.PlantingRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GardenBedService {

    private final GardenBedRepository beds;
    private final UserRepository users;
    private final PlantingRepository plantings;
    private final PlantingService plantingService;

    public GardenBedService(GardenBedRepository beds, UserRepository users,
                            PlantingRepository plantings, PlantingService plantingService) {
        this.beds = beds;
        this.users = users;
        this.plantings = plantings;
        this.plantingService = plantingService;
    }

    public List<GardenBed> findAll() {
        return beds.findAll();
    }

    public GardenBed findById(Long id) {
        return beds.findById(id).orElseThrow(() -> new NotFoundException("Грядка " + id + " не найдена"));
    }

    public GardenBed create(GardenBed data) {
        Validation.positive(data.area(), "area");
        Validation.required(data.ownerId(), "ownerId");
        checkOwner(data.ownerId());
        boolean available = data.available() == null || data.available();
        return beds.save(new GardenBed(null, data.area(), data.ownerId(), available), null);
    }

    public GardenBed update(Long id, GardenBed patch) {
        GardenBed current = findById(id);
        Double area = current.area();
        Long ownerId = current.ownerId();
        Boolean available = current.available();
        if (patch.area() != null) {
            Validation.positive(patch.area(), "area");
            area = patch.area();
        }
        if (patch.ownerId() != null) {
            checkOwner(patch.ownerId());
            ownerId = patch.ownerId();
        }
        if (patch.available() != null) {
            available = patch.available();
        }
        // нельзя уменьшить площадь меньше, чем уже занята посадками
        if (area < plantingService.occupiedArea(id, null)) {
            throw new BadRequestException("Площадь меньше, чем занята текущими посадками");
        }
        return beds.save(new GardenBed(id, area, ownerId, available), id);
    }

    /** Удаляет грядку и все её посадки (с урожаями). */
    public void delete(Long id) {
        findById(id);
        for (Planting p : plantings.findByGardenBedId(id)) {
            plantingService.delete(p.id());
        }
        beds.deleteById(id);
    }

    private void checkOwner(Long ownerId) {
        if (!users.existsById(ownerId)) {
            throw new BadRequestException("Владелец " + ownerId + " не существует");
        }
    }
}
