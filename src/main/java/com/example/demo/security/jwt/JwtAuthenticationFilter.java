package com.example.demo.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Аутентификация по access-токену из заголовка {@code Authorization: Bearer ...}.
 * <ol>
 *   <li>Достаёт токен из заголовка.</li>
 *   <li>Проверяет его (принимаются только access-токены; refresh здесь не подходит).</li>
 *   <li>Загружает пользователя из БД и проверяет, что учётная запись активна (заблокированный теряет доступ сразу).</li>
 *   <li>Кладёт аутентификацию в контекст безопасности запроса и передаёт запрос дальше.</li>
 * </ol>
 * Если токена нет или он недействителен, контекст остаётся пустым: защищённые адреса ответят 401
 * (это делает {@code ApiAuthenticationEntryPoint}), открытые адреса работают как обычно.
 * <p>
 * Фильтр намеренно НЕ объявлен как Spring-бин: иначе Spring Boot зарегистрирует его ещё и как обычный
 * фильтр сервлета, вне цепочки безопасности. Он создаётся в {@code SecurityConfig}.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER = "Bearer ";
    /** Эти адреса не используют access-токен, поэтому заголовок Authorization на них не разбирается. */
    private static final Set<String> TOKEN_FREE_POST = Set.of("/auth/login", "/auth/register", "/auth/refresh");

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return "POST".equals(request.getMethod()) && TOKEN_FREE_POST.contains(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.regionMatches(true, 0, BEARER, 0, BEARER.length())) {
            authenticate(request, header.substring(BEARER.length()).trim());
        }
        chain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String token) {
        try {
            Claims claims = tokenProvider.parseAccessToken(token);
            UserDetails user = userDetailsService.loadUserByUsername(claims.getSubject());
            if (!user.isEnabled() || !user.isAccountNonLocked()
                    || !user.isAccountNonExpired() || !user.isCredentialsNonExpired()) {
                SecurityContextHolder.clearContext();
                log.debug("Access-токен отклонён: учётная запись недоступна");
                return;
            }
            // полномочия берём из БД, а не из токена: смена роли действует без перевыпуска токена
            var authentication = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        } catch (InvalidTokenException | UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            // сам токен в журнал не пишем
            log.debug("Access-токен отклонён: {}", e.getClass().getSimpleName());
        }
    }
}
