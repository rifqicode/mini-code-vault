package com.example.minicodevault.service;

import com.example.minicodevault.model.ContentAccess;
import com.example.minicodevault.model.Learning;
import com.example.minicodevault.model.Order;
import com.example.minicodevault.model.User;
import com.example.minicodevault.repository.InMemoryStore;

public final class Authorization {
    private Authorization() {}

    public static void requireRole(User user, User.Role role) {
        if (user == null || user.role() != role) {
            throw new AppException(403, "Role Anda tidak memiliki akses ke fitur ini.");
        }
    }

    public static Learning learning(InMemoryStore store, long id) {
        return store.findLearning(id)
                .orElseThrow(() -> new AppException(404, "Course tidak ditemukan."));
    }

    public static Learning ownLearning(InMemoryStore store, User user, long id) {
        requireRole(user, User.Role.INSTRUCTOR);
        Learning course = learning(store, id);
        if (course.getInstructorId() != user.id())
            throw new AppException(403, "Course ini bukan milik Anda.");
        return course;
    }

    public static ContentAccess access(InMemoryStore store, User user, long id) {
        requireRole(user, User.Role.LEARNER);
        return store.findAccess(user.id(), id)
                .orElseThrow(() ->
                        new AppException(
                                403, "Beli dan selesaikan pembayaran course ini terlebih dahulu."));
    }

    public static Order order(InMemoryStore store, long id) {
        return store.findOrder(id)
                .orElseThrow(() -> new AppException(404, "Transaksi tidak ditemukan."));
    }
}
