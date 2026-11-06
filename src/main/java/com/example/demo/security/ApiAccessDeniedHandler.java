package com.example.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 403 Forbidden: пользователь вошёл, но у него недостаточно прав, либо отсутствует/неверен CSRF-токен.
 * Причина описана общими словами, без внутренних деталей.
 */
@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiAccessDeniedHandler.class);

    /** Убирает переводы строк и прочие управляющие символы, чтобы клиент не мог подделать записи журнала. */
    private static String clean(String value) {
        return value == null ? "-" : value.replaceAll("[\\p{Cntrl}]", "_");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        String message = accessDeniedException instanceof CsrfException
                ? "CSRF-токен отсутствует или недействителен. Получите токен запросом GET /api/csrf "
                  + "и передайте его в заголовке X-CSRF-TOKEN"
                : "Недостаточно прав для выполнения операции";
        // в журнал — кто и куда пытался попасть (пароли и токены не пишем)
        String who = request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "-";
        log.warn("Отказ 403 ({}): пользователь={} {} {}",
                accessDeniedException instanceof CsrfException ? "CSRF" : "нет прав",
                clean(who), clean(request.getMethod()), clean(request.getRequestURI()));
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
