package com.example.minicodevault.model;

public record User(long id, String name, String username, Role role) {
    public enum Role {
        INSTRUCTOR,
        LEARNER
    }
}
