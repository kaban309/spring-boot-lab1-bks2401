package com.example.demo.security;

import com.example.demo.security.jwt.JwtAuthenticationFilter;
import com.example.demo.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Настройка безопасности.
 * <ul>
 *   <li>Аутентификация — JWT: при входе или регистрации клиент получает пару access + refresh. Access короткоживущий
 *       и передаётся в заголовке {@code Authorization: Bearer ...}; refresh нужен только для обновления пары
 *       ({@code POST /auth/refresh}). Пароли в БД — BCrypt.</li>
 *   <li>Приложение не хранит HTTP-сессий (STATELESS): каждый запрос проверяется по access-токену. Серверный учёт
 *       выданных пар (таблица user_sessions) нужен только для обновления и отзыва refresh.</li>
 *   <li>CSRF-защита отключена осознанно. CSRF опирается на то, что браузер САМ прикладывает к запросу учётные данные
 *       (cookie, сессия, Basic). Access-токен браузер сам не подставляет: его в заголовок кладёт код клиента,
 *       поэтому чужой сайт отправить запрос от имени пользователя не может. Cookie с токенами не используются.</li>
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

    /** Менеджер аутентификации (DaoAuthenticationProvider: AppUserDetailsService + PasswordEncoder) для /auth/login. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ApiAuthenticationEntryPoint authenticationEntryPoint,
                                                   ApiAccessDeniedHandler accessDeniedHandler,
                                                   JwtTokenProvider jwtTokenProvider,
                                                   AppUserDetailsService userDetailsService) throws Exception {
        // не бин, чтобы Spring Boot не регистрировал фильтр второй раз вне цепочки безопасности
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);

        http
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)   // 401
                        .accessDeniedHandler(accessDeniedHandler))            // 403
                // access-токен разбирается до остальных проверок доступа
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // открытые адреса: учебные методы из лаб. 1, вход, регистрация, обновление пары, страница ошибок
                        .requestMatchers("/text", "/numbers", "/hello", "/add", "/umnozhenie").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register", "/auth/refresh").permitAll()
                        .requestMatchers("/error").permitAll()
                        // управление пользователями — только администратор (свой профиль — через @PreAuthorize)
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*").hasRole("ADMIN")
                        // справочник культур меняют только агроном и администратор
                        .requestMatchers(HttpMethod.POST, "/api/crops").hasAnyRole("AGRONOMIST", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/crops/*").hasAnyRole("AGRONOMIST", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/crops/*").hasAnyRole("AGRONOMIST", "ADMIN")
                        // всё остальное — только с действительным access-токеном
                        .anyRequest().authenticated());
        return http.build();
    }
}
