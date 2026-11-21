package com.example.demo.security.jwt;

import com.example.demo.exception.UnauthorizedException;

/**
 * Токен не прошёл проверку: подпись, срок действия, обязательные claims или тип не подходят.
 * Причина намеренно не раскрывается ни клиенту, ни в сообщении исключения.
 */
public class InvalidTokenException extends UnauthorizedException {
    public InvalidTokenException() {
        super("Токен недействителен");
    }
}
