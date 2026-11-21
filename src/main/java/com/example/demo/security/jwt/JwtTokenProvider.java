package com.example.demo.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Создание и проверка JWT. Оба вида токенов (access и refresh) — подписанные JWT (HS256).
 * <ul>
 *   <li>Общие claims: {@code iss}, {@code sub} (email), {@code jti} (новый для каждой выдачи),
 *       {@code iat}, {@code exp}, {@code type} ({@code access} или {@code refresh}).</li>
 *   <li>У access дополнительно {@code auth} — список ролей на момент выдачи. Для решений о доступе
 *       они не используются: роли берутся из БД, поэтому смена роли или блокировка действуют сразу.</li>
 * </ul>
 * Проверка каждого вида включает подпись, срок действия, издателя, алгоритм, наличие обязательных claims
 * и соответствие {@code type}: access не пройдёт проверку как refresh и наоборот.
 */
@Component
public class JwtTokenProvider {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private static final String ISSUER = "garden-app";
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_AUTH = "auth";
    /** Для HS256 нужен ключ не короче 256 бит. */
    private static final int MIN_KEY_BYTES = 32;

    private final SecretKey key;
    private final JwtParser parser;
    private final long accessTtlMillis;
    private final long refreshTtlMillis;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.accessExpiration}") long accessTtlMillis,
                            @Value("${jwt.refreshExpiration}") long refreshTtlMillis) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret.trim());
        } catch (RuntimeException e) {
            // значение ключа в сообщение не попадает
            throw new IllegalStateException("Настройка jwt.secret должна быть строкой в Base64 (см. .env.example)");
        }
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException("Ключ jwt.secret слишком короткий: нужно не менее 32 случайных байт");
        }
        if (accessTtlMillis <= 0 || refreshTtlMillis <= 0) {
            throw new IllegalStateException("Сроки жизни токенов (jwt.accessExpiration, jwt.refreshExpiration) "
                    + "должны быть положительными");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.parser = Jwts.parser().verifyWith(key).requireIssuer(ISSUER).build();
        this.accessTtlMillis = accessTtlMillis;
        this.refreshTtlMillis = refreshTtlMillis;
    }

    public IssuedToken createAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
        List<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).toList();
        return build(username, TYPE_ACCESS, roles, accessTtlMillis);
    }

    public IssuedToken createRefreshToken(String username) {
        return build(username, TYPE_REFRESH, null, refreshTtlMillis);
    }

    /** Проверяет access-токен. При любой проблеме бросает {@link InvalidTokenException}. */
    public Claims parseAccessToken(String token) {
        return parse(token, TYPE_ACCESS);
    }

    /** Проверяет refresh-токен. При любой проблеме бросает {@link InvalidTokenException}. */
    public Claims parseRefreshToken(String token) {
        return parse(token, TYPE_REFRESH);
    }

    private IssuedToken build(String username, String type, List<String> roles, long ttlMillis) {
        // JWT хранит время в секундах: работаем с секундной точностью, чтобы значение в БД
        // в точности соответствовало claim exp
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plusMillis(ttlMillis).truncatedTo(ChronoUnit.SECONDS);

        var builder = Jwts.builder()
                .issuer(ISSUER)
                .subject(username)
                .id(UUID.randomUUID().toString())          // jti: новый при каждой выдаче
                .claim(CLAIM_TYPE, type)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt));
        if (roles != null) {
            builder.claim(CLAIM_AUTH, roles);
        }
        String token = builder.signWith(key, Jwts.SIG.HS256).compact();
        return new IssuedToken(token, expiresAt);
    }

    private Claims parse(String token, String expectedType) {
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException();
        }
        try {
            // parseSignedClaims принимает только подписанные токены (alg=none отвергается),
            // проверяет подпись, exp, nbf и издателя
            Jws<Claims> jws = parser.parseSignedClaims(token.trim());
            if (!Jwts.SIG.HS256.getId().equals(jws.getHeader().getAlgorithm())) {
                throw new InvalidTokenException();
            }
            Claims claims = jws.getPayload();
            if (isBlank(claims.getSubject()) || isBlank(claims.getId())
                    || claims.getIssuedAt() == null || claims.getExpiration() == null) {
                throw new InvalidTokenException();
            }
            if (!expectedType.equals(claims.get(CLAIM_TYPE, String.class))) {
                throw new InvalidTokenException();
            }
            if (TYPE_ACCESS.equals(expectedType) && claims.get(CLAIM_AUTH, List.class) == null) {
                throw new InvalidTokenException();
            }
            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException();
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
