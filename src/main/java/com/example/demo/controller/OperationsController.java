package com.example.demo.controller;

import com.example.demo.dto.GardenBedDto;
import com.example.demo.dto.HarvestDto;
import com.example.demo.dto.OperationsDto;
import com.example.demo.dto.PlantingDto;
import com.example.demo.service.GardenOperationsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OperationsController {

    private final GardenOperationsService operations;

    public OperationsController(GardenOperationsService operations) {
        this.operations = operations;
    }

    @PostMapping("/garden-beds/{id}/plant")
    @ResponseStatus(HttpStatus.CREATED)
    public PlantingDto.Response plant(@PathVariable Long id, @RequestBody OperationsDto.PlantRequest request) {
        return PlantingDto.Response.from(operations.plant(id, request.cropId(), request.plantCount()));
    }

    @PostMapping("/plantings/{id}/harvest")
    @ResponseStatus(HttpStatus.CREATED)
    public HarvestDto.Response harvest(@PathVariable Long id, @RequestBody OperationsDto.HarvestRequest request) {
        return HarvestDto.Response.from(operations.harvest(id, request.mass(), request.finalHarvest()));
    }

    @PostMapping("/garden-beds/{id}/transfer")
    public GardenBedDto.Response transfer(@PathVariable Long id, @RequestBody OperationsDto.TransferRequest request) {
        return GardenBedDto.Response.from(operations.transfer(id, request.newOwnerId()));
    }

    @PostMapping("/plantings/{id}/move")
    public PlantingDto.Response move(@PathVariable Long id, @RequestBody OperationsDto.MoveRequest request) {
        return PlantingDto.Response.from(operations.move(id, request.targetBedId()));
    }

    @PostMapping("/garden-beds/{id}/close-season")
    public List<PlantingDto.Response> closeSeason(@PathVariable Long id) {
        return operations.closeSeason(id).stream().map(PlantingDto.Response::from).toList();
    }
}
