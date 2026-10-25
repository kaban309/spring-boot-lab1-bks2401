package com.example.demo.service;

import com.example.demo.exception.BadRequestException;

import java.math.BigDecimal;

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
        boolean ok = value != null && (value instanceof BigDecimal bd
                ? bd.signum() > 0
                : value.doubleValue() > 0);
        if (!ok) {
            throw new BadRequestException("Поле '" + field + "' должно быть положительным числом");
        }
    }

    /** Ограничение разрядности — чтобы значение влезло в NUMERIC(precision, scale) и не упало на уровне БД. */
    static void decimal(BigDecimal value, String field, int maxIntegerDigits, int maxScale) {
        positive(value, field);
        if (value.scale() > maxScale && value.stripTrailingZeros().scale() > maxScale) {
            throw new BadRequestException("Поле '" + field + "': не более " + maxScale + " знаков после запятой");
        }
        if (value.precision() - value.scale() > maxIntegerDigits) {
            throw new BadRequestException("Поле '" + field + "' слишком большое");
        }
    }
}
