package com.example.demo.repository;

import com.example.demo.model.UserSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    /**
     * Ищет сессию по refresh-токену и блокирует строку до конца транзакции (SELECT ... FOR UPDATE).
     * Если два запроса одновременно предъявят один и тот же refresh, второй дождётся первого
     * и увидит уже изменённый статус, поэтому обмен сможет выполнить только один из них.
     * Метод нужно вызывать внутри транзакции сервиса.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from UserSession s where s.refreshToken = :refreshToken")
    Optional<UserSession> findByRefreshTokenForUpdate(@Param("refreshToken") String refreshToken);
}
