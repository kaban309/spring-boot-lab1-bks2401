package com.example.demo.model;

/** Состояние сессии (одной выданной пары токенов). */
public enum SessionStatus {
    /** refresh ещё не использован и не отозван. */
    ACTIVE,
    /** refresh успешно применён для обновления пары. */
    USED,
    /** refresh отозван (например, после повторного предъявления уже использованного). */
    REVOKED
}
