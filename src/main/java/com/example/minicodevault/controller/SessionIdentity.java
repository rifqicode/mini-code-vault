package com.example.minicodevault.controller;

import com.example.minicodevault.model.User;
import com.example.minicodevault.service.AuthService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SessionIdentity {
    public static final String USER_ID = "userId";
    public static final String CSRF = "csrf";
    private final AuthService auth;

    public SessionIdentity(AuthService auth) {
        this.auth = auth;
    }

    public User current(HttpSession session) {
        return auth.user((Long) session.getAttribute(USER_ID));
    }

    public User optional(HttpSession session) {
        return session.getAttribute(USER_ID) == null ? null : current(session);
    }

    public String csrf(HttpSession session) {
        if (session.getAttribute(CSRF) == null)
            session.setAttribute(CSRF, UUID.randomUUID().toString());
        return (String) session.getAttribute(CSRF);
    }
}
