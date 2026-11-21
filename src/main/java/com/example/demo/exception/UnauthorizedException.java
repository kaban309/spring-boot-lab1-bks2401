package com.example.demo.exception;

/** Не удалось установить, кто делает запрос (неверный пароль, недействительный refresh) -> HTTP 401. */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
