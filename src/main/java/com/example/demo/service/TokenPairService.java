package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.model.SessionStatus;
import com.example.demo.model.User;
import com.example.demo.model.UserSession;
import com.example.demo.model.UserStatus;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.UserSessionRepository;
import com.example.demo.security.AppUserDetails;
import com.example.demo.security.jwt.IssuedToken;
import com.example.demo.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;

/**
 * Выдача и обновление пар токенов с серверным учётом сессий (таблица user_sessions).
 */
@Service
public class TokenPairService {

    private static final Logger log = LoggerFactory.getLogger(TokenPairService.class);

    /** Одно и то же сообщение для всех причин отказа: по ответу нельзя понять, что именно не так. */
    static final String REFRESH_DENIED = "Refresh-токен недействителен";
    private static final int MAX_REFRESH_LENGTH = 4096;

    private final JwtTokenProvider jwt;
    private final UserSessionRepository sessions;
    private final UserRepository users;

    public TokenPairService(JwtTokenProvider jwt, UserSessionRepository sessions, UserRepository users) {
        this.jwt = jwt;
        this.sessions = sessions;
        this.users = users;
    }

    /**
     * Выдача пары после успешного входа или создания учётной записи.
     * Пара возвращается только после того, как сессия сохранена в БД. Если сохранить не удалось,
     * бросается исключение и вызывающая транзакция откатывается (при регистрации вместе с пользователем).
     */
    @Transactional
    public TokenPair issue(String username, Collection<? extends GrantedAuthority> authorities, String deviceId) {
        return issuePair(username, authorities, deviceId);
    }

    /**
     * Обновление пары. Все шаги выполняются в одной транзакции:
     * <ol>
     *   <li>проверка refresh как JWT (подпись, срок, claims, type = refresh);</li>
     *   <li>поиск сессии со блокировкой строки;</li>
     *   <li>проверка владельца (sub), пользователя, статуса ACTIVE и срока refresh;</li>
     *   <li>старая сессия -> USED;</li>
     *   <li>новая пара и новая сессия ACTIVE (тот же пользователь и deviceId);</li>
     *   <li>фиксация транзакции и возврат новой пары.</li>
     * </ol>
     * Если новую пару сохранить не удалось, транзакция откатывается и старая сессия остаётся ACTIVE.
     * <p>
     * {@code noRollbackFor = UnauthorizedException}: отказ клиенту (401) сам по себе транзакцию не откатывает.
     * Это нужно, чтобы перевод повторно предъявленной сессии в REVOKED сохранился, несмотря на отказ в ответе.
     * Во всех остальных случаях отказа (неизвестная сессия, REVOKED, срок вышел) состояние не менялось,
     * откатывать нечего.
     */
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public TokenPair refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException("Поле 'refreshToken' обязательно");
        }
        if (refreshToken.length() > MAX_REFRESH_LENGTH) {
            throw new UnauthorizedException(REFRESH_DENIED);
        }

        // 1. refresh должен быть корректным JWT с type = refresh (иначе InvalidTokenException -> 401)
        Claims claims = jwt.parseRefreshToken(refreshToken);

        // 2. сессия, выданная вместе с этим refresh; строка блокируется до конца транзакции
        UserSession session = sessions.findByRefreshTokenForUpdate(refreshToken.trim())
                .orElseThrow(() -> new UnauthorizedException(REFRESH_DENIED));

        // 3. владелец записи совпадает с sub токена
        if (!session.getUsername().equals(claims.getSubject())) {
            throw new UnauthorizedException(REFRESH_DENIED);
        }
        Instant now = Instant.now();

        if (session.getStatus() == SessionStatus.REVOKED) {
            throw new UnauthorizedException(REFRESH_DENIED);
        }
        if (session.getStatus() == SessionStatus.USED) {
            // refresh уже был обменян, а его предъявляют снова: возможно, токен украден.
            // Ещё не истёкший refresh отзываем; изменение сохранится, несмотря на отказ (noRollbackFor).
            if (session.getRefreshTokenExpiry().isAfter(now)) {
                session.setStatus(SessionStatus.REVOKED);
                sessions.saveAndFlush(session);
                log.warn("Повторное предъявление использованного refresh: сессия {} отозвана", session.getId());
            }
            throw new UnauthorizedException(REFRESH_DENIED);
        }
        // статус ACTIVE и срок refresh не истёк
        if (!session.getRefreshTokenExpiry().isAfter(now)) {
            throw new UnauthorizedException(REFRESH_DENIED);
        }
        // пользователь существует и активен (заблокированному пару не обновляем)
        User user = users.findByEmail(session.getUsername())
                .orElseThrow(() -> new UnauthorizedException(REFRESH_DENIED));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(REFRESH_DENIED);
        }

        // 4. старая сессия -> USED
        session.setStatus(SessionStatus.USED);
        sessions.saveAndFlush(session);

        // 5. новая пара и новая сессия ACTIVE с тем же пользователем и deviceId;
        //    любое исключение отсюда откатит и перевод старой сессии в USED
        TokenPair pair = issuePair(user.getEmail(), new AppUserDetails(user).getAuthorities(), session.getDeviceId());

        // 6. транзакция фиксируется при выходе из метода, клиент получает новую пару
        return pair;
    }

    private TokenPair issuePair(String username, Collection<? extends GrantedAuthority> authorities,
                                String deviceId) {
        IssuedToken access = jwt.createAccessToken(username, authorities);
        IssuedToken refresh = jwt.createRefreshToken(username);
        UserSession session = new UserSession(username, deviceId,
                access.value(), access.expiresAt(),
                refresh.value(), refresh.expiresAt(),
                SessionStatus.ACTIVE);
        // saveAndFlush: ошибка БД проявится здесь, а не при фиксации, уже после возврата пары
        sessions.saveAndFlush(session);
        return new TokenPair(access.value(), refresh.value());
    }
}
