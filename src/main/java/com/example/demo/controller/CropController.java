package com.example.demo.controller;

import com.example.demo.dto.CropDto;
import com.example.demo.service.CropService;
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
    public CropDto.Response create(@RequestBody CropDto.Request request) {
        return CropDto.Response.from(service.create(request));
    }

    @PatchMapping("/{id}")
    public CropDto.Response update(@PathVariable Long id, @RequestBody CropDto.Request request) {
        return CropDto.Response.from(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
