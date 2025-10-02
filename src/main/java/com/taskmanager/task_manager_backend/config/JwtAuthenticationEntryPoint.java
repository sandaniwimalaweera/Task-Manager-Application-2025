package com.taskmanager.task_manager_backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // Set response status to 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Create JSON error response
        String jsonResponse = "{\n" +
                "  \"error\": \"Unauthorized\",\n" +
                "  \"message\": \"Authentication required to access this resource\",\n" +
                "  \"status\": 401,\n" +
                "  \"timestamp\": \"" + java.time.Instant.now() + "\",\n" +
                "  \"path\": \"" + request.getRequestURI() + "\"\n" +
                "}";

        response.getWriter().write(jsonResponse);
    }
}