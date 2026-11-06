package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Настройка безопасности.
 * <ul>
 *   <li>Аутентификация — HTTP Basic (логин = email), пароли в БД хранятся как BCrypt.</li>
 *   <li>Приложение не хранит сессий (STATELESS): каждый запрос проверяется по заголовку Authorization,
 *       «запомненных» пользователей нет.</li>
 *   <li>CSRF-защита включена для всех запросов, изменяющих данные (POST/PATCH/DELETE). Токен выдаёт GET /api/csrf
 *       (одновременно в cookie XSRF-TOKEN), клиент передаёт его в заголовке X-CSRF-TOKEN; сервер сверяет заголовок
 *       с cookie.</li>
 *   <li>Авторизация — два уровня: грубые правила по URL здесь + точные правила (владелец, роль) через
 *       {@code @PreAuthorize} на методах контроллеров.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ApiAuthenticationEntryPoint authenticationEntryPoint,
                                                   ApiAccessDeniedHandler accessDeniedHandler) throws Exception {
        // Токен хранится в cookie XSRF-TOKEN, а клиент присылает его в заголовке X-CSRF-TOKEN
        CookieCsrfTokenRepository csrfRepository = new CookieCsrfTokenRepository();
        csrfRepository.setHeaderName("X-CSRF-TOKEN");

        http
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CSRF включён: POST/PATCH/DELETE без верного токена получают 403. Токен хранится в cookie XSRF-TOKEN
                .csrf(csrf -> csrf.csrfTokenRepository(csrfRepository))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)   // 401
                        .accessDeniedHandler(accessDeniedHandler))            // 403
                .authorizeHttpRequests(auth -> auth
                        // открытые эндпоинты: учебные методы из лаб. 1, токен CSRF, регистрация, страница ошибок
                        .requestMatchers("/text", "/numbers", "/hello", "/add", "/umnozhenie").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers("/error").permitAll()
                        // управление пользователями — только администратор (свой профиль — через @PreAuthorize)
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN")
                        // справочник культур меняют только агроном и администратор
                        .requestMatchers(HttpMethod.POST, "/api/crops").hasAnyRole("AGRONOMIST", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/crops/*").hasAnyRole("AGRONOMIST", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/crops/*").hasAnyRole("AGRONOMIST", "ADMIN")
                        // всё остальное — только для вошедших пользователей
                        .anyRequest().authenticated());
        return http.build();
    }
}
