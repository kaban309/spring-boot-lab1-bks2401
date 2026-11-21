package com.example.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 403 Forbidden: пользователь аутентифицирован (access-токен действителен), но у него недостаточно прав.
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
        // в журнал — кто и куда пытался попасть (пароли и токены не пишем)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String who = authentication != null ? authentication.getName() : "-";
        log.warn("Отказ 403 (нет прав): пользователь={} {} {}",
                clean(who), clean(request.getMethod()), clean(request.getRequestURI()));
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"Недостаточно прав для выполнения операции\"}");
    }
}
