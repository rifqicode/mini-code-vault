package com.example.minicodevault.service;

import com.example.minicodevault.model.User;
import com.example.minicodevault.repository.InMemoryStore;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final String DEMO_PASSWORD = "demo123";
    private final InMemoryStore store;

    public AuthService(InMemoryStore store) {
        this.store = store;
    }

    public User login(String username, String password) {
        if (!DEMO_PASSWORD.equals(password))
            throw new AppException(401, "Username atau password salah.");
        return store.findUser(username)
                .orElseThrow(() -> new AppException(401, "Username atau password salah."));
    }

    public User user(Long id) {
        if (id == null) throw new AppException(401, "Silakan masuk terlebih dahulu.");
        return store.findUser(id)
                .orElseThrow(() -> new AppException(401, "Silakan masuk terlebih dahulu."));
    }
}
