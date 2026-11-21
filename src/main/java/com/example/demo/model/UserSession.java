package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Серверный учёт выданной пары токенов. Одна запись = одна пара access + refresh.
 * При обновлении пары старая запись остаётся (статус USED), а для новой пары создаётся новая строка.
 */
@Entity
@Table(name = "user_sessions")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    /** Владелец пары: email пользователя (он же sub в токенах). */
    @Column(name = "username", nullable = false)
    private String username;

    /** Метка клиента, задаётся самим клиентом и подлинность устройства не подтверждает. Может быть null. */
    @Column(name = "device_id", length = 128)
    private String deviceId;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", nullable = false, unique = true, columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "access_token_expiry", nullable = false)
    private Instant accessTokenExpiry;

    @Column(name = "refresh_token_expiry", nullable = false)
    private Instant refreshTokenExpiry;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private SessionStatus status;

    protected UserSession() {
    }

    public UserSession(String username, String deviceId,
                       String accessToken, Instant accessTokenExpiry,
                       String refreshToken, Instant refreshTokenExpiry,
                       SessionStatus status) {
        this.username = username;
        this.deviceId = deviceId;
        this.accessToken = accessToken;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshToken = refreshToken;
        this.refreshTokenExpiry = refreshTokenExpiry;
        this.status = status;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getDeviceId() { return deviceId; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public Instant getAccessTokenExpiry() { return accessTokenExpiry; }
    public Instant getRefreshTokenExpiry() { return refreshTokenExpiry; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
}
