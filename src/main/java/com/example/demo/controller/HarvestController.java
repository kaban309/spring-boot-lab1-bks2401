package com.example.demo.controller;

import com.example.demo.dto.HarvestDto;
import com.example.demo.service.HarvestService;
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
@RequestMapping("/api/harvests")
public class HarvestController {

    private final HarvestService service;

    public HarvestController(HarvestService service) {
        this.service = service;
    }

    @GetMapping
    public List<HarvestDto.Response> getAll() {
        return service.findAll().stream().map(HarvestDto.Response::from).toList();
    }

    @GetMapping("/{id}")
    public HarvestDto.Response getById(@PathVariable Long id) {
        return HarvestDto.Response.from(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HarvestDto.Response create(@RequestBody HarvestDto.Request request) {
        return HarvestDto.Response.from(service.create(request));
    }

    @PatchMapping("/{id}")
    public HarvestDto.Response update(@PathVariable Long id, @RequestBody HarvestDto.Request request) {
        return HarvestDto.Response.from(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
