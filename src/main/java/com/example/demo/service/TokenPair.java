package com.example.demo.service;

/** Пара токенов, возвращаемая клиенту. */
public record TokenPair(String accessToken, String refreshToken) {
}
