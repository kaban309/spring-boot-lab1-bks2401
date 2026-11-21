package com.example.demo.security.jwt;

import java.time.Instant;

/**
 * Выданный токен: строка JWT и момент истечения, ровно совпадающий с claim exp (с точностью до секунды).
 * Именно это значение сохраняется в сессии.
 */
public record IssuedToken(String value, Instant expiresAt) {
}
