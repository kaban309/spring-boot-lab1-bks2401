package com.example.demo.service;

import com.example.demo.exception.BadRequestException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Проверка надёжности пароля при регистрации и смене пароля. Пароль в сообщения об ошибках не попадает. */
public final class PasswordPolicy {

    static final int MIN_LENGTH = 10;
    static final int MAX_LENGTH = 64;
    /** BCrypt учитывает только первые 72 байта пароля — более длинные пароли не принимаем. */
    static final int MAX_BYTES = 72;

    private static final Set<String> COMMON_PARTS = Set.of(
            "password", "qwerty", "123456", "admin123", "letmein", "welcome", "iloveyou", "пароль", "йцукен");

    private PasswordPolicy() {
    }

    public static void validate(String password, String email) {
        if (password == null || password.isEmpty()) {
            throw new BadRequestException("Поле 'password' обязательно");
        }
        List<String> problems = new ArrayList<>();
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            problems.add("длина от " + MIN_LENGTH + " до " + MAX_LENGTH + " символов");
        } else if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            problems.add("не более " + MAX_BYTES + " байт (кириллица занимает по 2 байта)");
        }
        if (password.chars().anyMatch(Character::isWhitespace)) {
            problems.add("без пробелов");
        }
        if (password.chars().noneMatch(Character::isLowerCase)) {
            problems.add("хотя бы одна строчная буква");
        }
        if (password.chars().noneMatch(Character::isUpperCase)) {
            problems.add("хотя бы одна заглавная буква");
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            problems.add("хотя бы одна цифра");
        }
        if (password.chars().allMatch(c -> Character.isLetterOrDigit(c))) {
            problems.add("хотя бы один специальный символ (например, # ! ? $ %)");
        }
        String lower = password.toLowerCase(Locale.ROOT);
        if (COMMON_PARTS.stream().anyMatch(lower::contains)) {
            problems.add("не должен содержать распространённые слова вроде 'password' или 'qwerty'");
        }
        if (email != null) {
            String local = email.trim().toLowerCase(Locale.ROOT).split("@", 2)[0];
            if (local.length() >= 3 && lower.contains(local)) {
                problems.add("не должен содержать часть email");
            }
        }
        if (!problems.isEmpty()) {
            throw new BadRequestException("Слишком слабый пароль: " + String.join("; ", problems));
        }
    }
}
