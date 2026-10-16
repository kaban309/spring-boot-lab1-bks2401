package com.example.demo.service;

import com.example.demo.exception.BadRequestException;

/** Общие проверки входных данных. */
final class Validation {
    private Validation() {}

    static void required(Object value, String field) {
        if (value == null) {
            throw new BadRequestException("Поле '" + field + "' обязательно");
        }
    }

    static void text(String value, String field, int maxLen) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Поле '" + field + "' обязательно");
        }
        if (value.length() > maxLen) {
            throw new BadRequestException("Поле '" + field + "' длиннее " + maxLen + " символов");
        }
    }

    static void positive(Number value, String field) {
        if (value == null || !(value.doubleValue() > 0) || Double.isInfinite(value.doubleValue())) {
            throw new BadRequestException("Поле '" + field + "' должно быть положительным числом");
        }
    }
}
