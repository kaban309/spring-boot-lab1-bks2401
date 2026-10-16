package com.example.demo.exception;

/** Некорректные данные запроса -> HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
