package com.example.demo.controller;

import com.example.demo.dto.PlantingDto;
import com.example.demo.model.Planting;
import com.example.demo.service.PlantingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plantings")
public class PlantingController {

    private final PlantingService service;

    public PlantingController(PlantingService service) {
        this.service = service;
    }

    @GetMapping
    public List<PlantingDto.Response> getAll() {
        return service.findAll().stream().map(PlantingDto.Response::from).toList();
    }

    @GetMapping("/{id}")
    public PlantingDto.Response getById(@PathVariable Long id) {
        return PlantingDto.Response.from(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlantingDto.Response create(@RequestBody PlantingDto.Request r) {
        return PlantingDto.Response.from(service.create(toEntity(r)));
    }

    @PatchMapping("/{id}")
    public PlantingDto.Response update(@PathVariable Long id, @RequestBody PlantingDto.Request r) {
        return PlantingDto.Response.from(service.update(id, toEntity(r)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private Planting toEntity(PlantingDto.Request r) {
        return new Planting(null, r.gardenBedId(), r.cropId(), r.plantCount(), r.status());
    }
}
