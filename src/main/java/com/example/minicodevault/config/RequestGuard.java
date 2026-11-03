package com.example.minicodevault.config;

import com.example.minicodevault.controller.SessionIdentity;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class RequestGuard extends OncePerRequestFilter {
    private static final Set<String> SAFE = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "same-origin");
        response.setHeader(
                "Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:;"
                        + " media-src 'self'; connect-src 'self'; frame-ancestors 'none'; base-uri"
                        + " 'self'; form-action 'self'");
        response.setHeader("Cache-Control", "no-store");
        
        if (!SAFE.contains(request.getMethod())) {
            HttpSession session = request.getSession(false);
            Object token = session == null ? null : session.getAttribute(SessionIdentity.CSRF);
            String submitted = request.getHeader("X-CSRF-Token");
            if (submitted == null) submitted = request.getParameter("_csrf");
            if (token == null || !token.equals(submitted)) {
                if (!request.getRequestURI().startsWith("/api/")) {
                    response.sendRedirect("/login?expired=true");
                    return;
                }
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter()
                        .write(
                                "{\"error\":\"Session berubah. Muat ulang halaman lalu coba"
                                        + " lagi.\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
