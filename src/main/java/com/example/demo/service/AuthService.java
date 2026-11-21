package com.example.demo.service;

import com.example.demo.dto.UserDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.User;
import com.example.demo.security.AppUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/** Вход, регистрация и обновление токенов (сценарии /auth/login, /auth/register, /auth/refresh). */
@Service
public class AuthService {

    /** Одинаково для неверного пароля, несуществующего email и заблокированной учётной записи. */
    private static final String LOGIN_DENIED = "Неверный email или пароль, либо учётная запись недоступна";
    private static final int MAX_DEVICE_ID = 128;
    /** Длиннее пароль заведомо неверный: правило регистрации допускает не более 64 символов. */
    private static final int MAX_PASSWORD = 64;

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenPairService tokens;

    public AuthService(AuthenticationManager authenticationManager, UserService userService,
                       TokenPairService tokens) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.tokens = tokens;
    }

    /** Проверяет логин и пароль и выдаёт пару токенов. Неверные данные -> 401, пустые поля -> 400. */
    public TokenPair login(String email, String password, String deviceId) {
        Validation.text(email, "email", 255);
        Validation.text(password, "password", 1000);
        String device = normalizeDeviceId(deviceId);
        if (password.length() > MAX_PASSWORD) {
            throw new UnauthorizedException(LOGIN_DENIED);
        }
        Authentication authentication;
        try {
            // DaoAuthenticationProvider: UserDetailsService + BCrypt; заблокированная учётная запись не проходит
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            email.trim().toLowerCase(Locale.ROOT), password));
        } catch (InternalAuthenticationServiceException e) {
            throw e;                                   // сбой, а не неверные данные: это не 401
        } catch (AuthenticationException e) {
            throw new UnauthorizedException(LOGIN_DENIED);
        }
        AppUserDetails user = (AppUserDetails) authentication.getPrincipal();
        return tokens.issue(user.getUsername(), user.getAuthorities(), device);
    }

    /**
     * Создаёт учётную запись и сразу выдаёт пару токенов. Это ОДНА транзакция: если сессию сохранить
     * не удалось, откатывается и создание пользователя, а клиент не получает успешный ответ с токенами.
     * Все прежние проверки сохранены: обязательные поля, надёжность пароля, уникальность email;
     * роль самому себе назначить нельзя (регистрация всегда создаёт активного садовода).
     */
    @Transactional
    public TokenPair register(String name, String email, String password, String deviceId) {
        String device = normalizeDeviceId(deviceId);
        User user = userService.register(new UserDto.RegisterRequest(name, email, password));
        return tokens.issue(user.getEmail(), new AppUserDetails(user).getAuthorities(), device);
    }

    /** Обмен действующего refresh на новую пару. Подробности — в {@link TokenPairService#refresh}. */
    public TokenPair refresh(String refreshToken) {
        return tokens.refresh(refreshToken);
    }

    private static String normalizeDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            return null;                               // deviceId необязателен
        }
        String d = deviceId.trim();
        if (d.length() > MAX_DEVICE_ID) {
            throw new BadRequestException("Поле 'deviceId' длиннее " + MAX_DEVICE_ID + " символов");
        }
        if (d.chars().anyMatch(Character::isISOControl)) {
            throw new BadRequestException("Поле 'deviceId' содержит недопустимые символы");
        }
        return d;
    }
}
