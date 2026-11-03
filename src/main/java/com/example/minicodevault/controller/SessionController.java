package com.example.minicodevault.controller;

import com.example.minicodevault.dto.Views;
import com.example.minicodevault.model.User;
import com.example.minicodevault.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SessionController {
    public record Credentials(String username, String password) {}

    private final AuthService auth;
    private final SessionIdentity identity;

    public SessionController(AuthService auth, SessionIdentity identity) {
        this.auth = auth;
        this.identity = identity;
    }

    @GetMapping("/session")
    public Views.SessionView session(HttpSession session) {
        return new Views.SessionView(identity.csrf(session), identity.optional(session));
    }

    @PostMapping("/login")
    public User login(@RequestBody Credentials input, HttpServletRequest request) {
        User user = auth.login(input.username(), input.password());
        request.changeSessionId();
        request.getSession().setAttribute(SessionIdentity.USER_ID, user.id());
        return user;
    }

    @PostMapping("/logout")
    public Views.Result logout(HttpSession session) {
        session.invalidate();
        return Views.Result.success();
    }
}
