package com.example.demo.controller;

import com.example.demo.dto.CropDto;
import com.example.demo.model.Crop;
import com.example.demo.service.CropService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    private final CropService service;

    public CropController(CropService service) {
        this.service = service;
    }

    @GetMapping
    public List<CropDto.Response> getAll() {
        return service.findAll().stream().map(CropDto.Response::from).toList();
    }

    @GetMapping("/{id}")
    public CropDto.Response getById(@PathVariable Long id) {
        return CropDto.Response.from(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CropDto.Response create(@RequestBody CropDto.Request r) {
        return CropDto.Response.from(service.create(toEntity(r)));
    }

    @PatchMapping("/{id}")
    public CropDto.Response update(@PathVariable Long id, @RequestBody CropDto.Request r) {
        return CropDto.Response.from(service.update(id, toEntity(r)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private Crop toEntity(CropDto.Request r) {
        return new Crop(null, r.name(), r.season(), r.minAreaPerPlant());
    }
}
