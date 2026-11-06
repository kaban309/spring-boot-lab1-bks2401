package com.example.demo.controller;

import com.example.demo.dto.UserDto;
import com.example.demo.security.AppUserDetails;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }


    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of(
                "headerName", token.getHeaderName(),
                "parameterName", token.getParameterName(),
                "token", token.getToken());
    }

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto.Response register(@RequestBody UserDto.RegisterRequest request) {
        return UserDto.Response.from(users.register(request));
    }

    @GetMapping("/auth/me")
    @PreAuthorize("isAuthenticated()")
    public UserDto.Response me(@AuthenticationPrincipal AppUserDetails me) {
        return UserDto.Response.from(users.findById(me.getId()));
    }
}
