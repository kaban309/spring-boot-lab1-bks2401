package com.example.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 401 Unauthorized: access-токен не предъявлен, недействителен (подпись, срок, тип) или учётная запись недоступна
 * (заблокирована, удалена). Сообщение намеренно одинаковое для всех случаев — по ответу нельзя понять причину.
 */
@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(ApiAuthenticationEntryPoint.class);

    /** Убирает переводы строк и прочие управляющие символы, чтобы клиент не мог подделать записи журнала. */
    private static String clean(String value) {
        return value == null ? "-" : value.replaceAll("[\\p{Cntrl}]", "_");
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // в журнал — только факт и адрес; токены и пароли не записываются
        log.warn("Отказ 401: {} {} ({})", clean(request.getMethod()), clean(request.getRequestURI()),
                authException.getClass().getSimpleName());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("WWW-Authenticate", "Bearer realm=\"garden\"");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"Требуется аутентификация: передайте действующий access-токен "
                + "в заголовке Authorization: Bearer\"}");
    }
}
