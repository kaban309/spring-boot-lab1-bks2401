package com.example.demo.controller;

import com.example.demo.dto.UserDto;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserDto.Response> getAll() {
        return service.findAll().stream().map(UserDto.Response::from).toList();
    }

    @GetMapping("/{id}")
    public UserDto.Response getById(@PathVariable Long id) {
        return UserDto.Response.from(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto.Response create(@RequestBody UserDto.Request r) {
        return UserDto.Response.from(service.create(toEntity(r)));
    }

    @PatchMapping("/{id}")
    public UserDto.Response update(@PathVariable Long id, @RequestBody UserDto.Request r) {
        return UserDto.Response.from(service.update(id, toEntity(r)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    private User toEntity(UserDto.Request r) {
        return new User(null, r.name(), r.email());
    }
}
