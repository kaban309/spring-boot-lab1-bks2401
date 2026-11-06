package com.example.demo.controller;

import com.example.demo.dto.GardenBedDto;
import com.example.demo.security.AppUserDetails;
import com.example.demo.service.GardenBedService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/garden-beds")
public class GardenBedController {

    private final GardenBedService service;

    public GardenBedController(GardenBedService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<GardenBedDto.Response> getAll(@AuthenticationPrincipal AppUserDetails me) {
        return (me.isAdmin() ? service.findAll() : service.findAllOwnedBy(me.getId())).stream().map(GardenBedDto.Response::from).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @access.ownsBed(#id)")
    public GardenBedDto.Response getById(@PathVariable Long id) {
        return GardenBedDto.Response.from(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or @access.isSelf(#request.ownerId())")
    public GardenBedDto.Response create(@RequestBody GardenBedDto.Request request) {
        return GardenBedDto.Response.from(service.create(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (@access.ownsBed(#id) and (#request.ownerId() == null or @access.isSelf(#request.ownerId())))")
    public GardenBedDto.Response update(@PathVariable Long id, @RequestBody GardenBedDto.Request request) {
        return GardenBedDto.Response.from(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or @access.ownsBed(#id)")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
