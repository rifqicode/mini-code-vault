package com.example.minicodevault.repository;

import com.example.minicodevault.model.ContentAccess;
import com.example.minicodevault.model.Learning;
import com.example.minicodevault.model.Order;
import com.example.minicodevault.model.Review;
import com.example.minicodevault.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public final class InMemoryStore {
    private final Map<Long, User> users = new LinkedHashMap<>();
    private final Map<Long, Learning> learning = new LinkedHashMap<>();
    private final Map<Long, Set<Long>> carts = new HashMap<>();
    private final Map<Long, Order> orders = new LinkedHashMap<>();
    private final Map<Long, Map<Long, ContentAccess>> access = new HashMap<>();
    private final Map<ReviewKey, Review> reviews = new LinkedHashMap<>();
    private long learningId = 4;
    private long orderId = 1001;

    private record ReviewKey(long learnerId, long learningId) {}

    public synchronized <T> T transaction(Supplier<T> operation) {
        return operation.get();
    }

    public synchronized Optional<User> findUser(long id) {
        return Optional.ofNullable(users.get(id));
    }

    public synchronized Optional<User> findUser(String username) {
        return users.values().stream().filter(user -> user.username().equals(username)).findFirst();
    }

    public synchronized void saveUser(User user) {
        users.put(user.id(), user);
    }

    public synchronized Optional<Learning> findLearning(long id) {
        return Optional.ofNullable(learning.get(id));
    }

    public synchronized List<Learning> allLearning() {
        return List.copyOf(learning.values());
    }

    public synchronized void saveLearning(Learning course) {
        learning.put(course.getId(), course);
    }

    public synchronized List<Long> cartIds(long learnerId) {
        return new ArrayList<>(carts.getOrDefault(learnerId, Set.of()));
    }

    public synchronized boolean addCart(long learnerId, long courseId) {
        return carts.computeIfAbsent(learnerId, ignored -> new LinkedHashSet<>()).add(courseId);
    }

    public synchronized void removeCart(long learnerId, long courseId) {
        Set<Long> cart = carts.get(learnerId);
        if (cart != null) cart.remove(courseId);
    }

    public synchronized void clearCart(long learnerId) {
        carts.remove(learnerId);
    }

    public synchronized void saveOrder(Order order) {
        orders.put(order.getId(), order);
    }

    public synchronized Optional<Order> findOrder(long id) {
        return Optional.ofNullable(orders.get(id));
    }

    public synchronized List<Order> allOrders() {
        return List.copyOf(orders.values());
    }

    public synchronized Optional<ContentAccess> findAccess(long learnerId, long courseId) {
        return Optional.ofNullable(access.getOrDefault(learnerId, Map.of()).get(courseId));
    }

    public synchronized List<ContentAccess> allAccess(long learnerId) {
        return List.copyOf(access.getOrDefault(learnerId, Map.of()).values());
    }

    public synchronized void grantAccess(
            long learnerId, Learning course, java.time.Instant paidAt) {
        access.computeIfAbsent(learnerId, ignored -> new LinkedHashMap<>())
                .putIfAbsent(course.getId(), new ContentAccess(course, paidAt));
    }

    public synchronized void saveReview(Review review) {
        reviews.put(new ReviewKey(review.learnerId(), review.learningId()), review);
    }

    public synchronized Review findReview(long learnerId, long courseId) {
        return reviews.get(new ReviewKey(learnerId, courseId));
    }

    public synchronized List<Review> reviewsFor(long courseId) {
        return reviews.values().stream().filter(review -> review.learningId() == courseId).toList();
    }

    public synchronized List<Review> allReviews() {
        return List.copyOf(reviews.values());
    }

    public synchronized long nextLearningId() {
        return learningId++;
    }

    public synchronized long nextOrderId() {
        return orderId++;
    }
}
