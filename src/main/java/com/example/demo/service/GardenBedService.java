package com.example.demo.service;

import com.example.demo.dto.GardenBedDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.GardenBed;
import com.example.demo.model.User;
import com.example.demo.repository.GardenBedRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GardenBedService {

    private final GardenBedRepository beds;
    private final UserRepository users;
    private final GardenAreaCalculator calculator;
    private final GardenOperationsService operations;

    public GardenBedService(GardenBedRepository beds, UserRepository users,
                            GardenAreaCalculator calculator, GardenOperationsService operations) {
        this.beds = beds;
        this.users = users;
        this.calculator = calculator;
        this.operations = operations;
    }

    @Transactional(readOnly = true)
    public List<GardenBed> findAll() {
        return beds.findAll();
    }

    @Transactional(readOnly = true)
    public GardenBed findById(Long id) {
        return beds.findById(id).orElseThrow(() -> new NotFoundException("Грядка " + id + " не найдена"));
    }

    @Transactional
    public GardenBed create(GardenBedDto.Request r) {
        Validation.decimal(r.area(), "area", 6, 2);
        Validation.required(r.ownerId(), "ownerId");
        User owner = users.findById(r.ownerId())
                .orElseThrow(() -> new BadRequestException("Владелец " + r.ownerId() + " не существует"));
        boolean available = r.available() == null || r.available();
        return beds.save(new GardenBed(r.area(), owner, available));
    }

    @Transactional
    public GardenBed update(Long id, GardenBedDto.Request r) {
        GardenBed bed = beds.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Грядка " + id + " не найдена"));
        if (r.ownerId() != null && !r.ownerId().equals(bed.getOwner().getId())) {
            // смена владельца подчиняется тем же правилам, что и бизнес-операция «передать грядку»
            operations.transfer(id, r.ownerId());
        }
        if (r.area() != null) {
            Validation.decimal(r.area(), "area", 6, 2);
            if (r.area().compareTo(calculator.occupied(id, null)) < 0) {
                throw new ConflictException("Площадь меньше, чем занята текущими посадками");
            }
            bed.setArea(r.area());
        }
        if (r.available() != null) {
            bed.setAvailable(r.available());
        }
        return bed;
    }

    /** Удаляет грядку вместе с посадками и сборами. */
    @Transactional
    public void delete(Long id) {
        beds.delete(findById(id));
    }
}
