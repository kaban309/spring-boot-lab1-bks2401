package com.example.demo.controller;

import com.example.demo.dto.AuthDto;
import com.example.demo.dto.UserDto;
import com.example.demo.security.AppUserDetails;
import com.example.demo.service.AuthService;
import com.example.demo.service.TokenPair;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Аутентификация по JWT. Контроллер только принимает запрос и формирует ответ,
 * вся логика (проверка пароля, токены, сессии, транзакции) — в сервисах.
 * <ul>
 *   <li>POST /auth/login    — проверить логин и пароль, выдать пару токенов (200);</li>
 *   <li>POST /auth/register — создать учётную запись и сразу выдать пару (201);</li>
 *   <li>POST /auth/refresh  — обменять действующий refresh на новую пару (200), access не нужен;</li>
 *   <li>GET  /auth/me       — кто я (защищённый адрес, нужен access в заголовке Authorization: Bearer).</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService auth;
    private final UserService users;

    public AuthController(AuthService auth, UserService users) {
        this.auth = auth;
        this.users = users;
    }

    @PostMapping("/login")
    public AuthDto.TokenResponse login(@RequestBody AuthDto.LoginRequest request) {
        return toResponse(auth.login(request.email(), request.password(), request.deviceId()));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthDto.TokenResponse register(@RequestBody AuthDto.RegisterRequest request) {
        return toResponse(auth.register(request.name(), request.email(), request.password(), request.deviceId()));
    }

    @PostMapping("/refresh")
    public AuthDto.TokenResponse refresh(@RequestBody AuthDto.RefreshRequest request) {
        return toResponse(auth.refresh(request.refreshToken()));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserDto.Response me(@AuthenticationPrincipal AppUserDetails me) {
        return UserDto.Response.from(users.findById(me.getId()));
    }

    private static AuthDto.TokenResponse toResponse(TokenPair pair) {
        return new AuthDto.TokenResponse(pair.accessToken(), pair.refreshToken());
    }
}
