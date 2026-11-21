package com.example.demo.dto;

/** Тела запросов и ответов контроллера аутентификации. */
public final class AuthDto {
    private AuthDto() {}

    /** deviceId — необязательная метка клиента (до 128 символов). */
    public record LoginRequest(String email, String password, String deviceId) {}

    /** Роль и статус в запросе не читаются: новый пользователь всегда садовод. */
    public record RegisterRequest(String name, String email, String password, String deviceId) {}

    public record RefreshRequest(String refreshToken) {}

    /** Ответ всех трёх операций: только пара токенов. */
    public record TokenResponse(String accessToken, String refreshToken) {}
}
