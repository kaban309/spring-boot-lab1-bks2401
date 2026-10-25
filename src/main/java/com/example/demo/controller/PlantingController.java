package com.example.demo.controller;

import com.example.demo.dto.PlantingDto;
import com.example.demo.service.PlantingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    public PlantingDto.Response create(@RequestBody PlantingDto.Request request) {
        return PlantingDto.Response.from(service.create(request));
    }

    @PatchMapping("/{id}")
    public PlantingDto.Response update(@PathVariable Long id, @RequestBody PlantingDto.Request request) {
        return PlantingDto.Response.from(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
