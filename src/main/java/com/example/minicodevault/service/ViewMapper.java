package com.example.minicodevault.service;

import com.example.minicodevault.dto.Views;
import com.example.minicodevault.model.ContentAccess;
import com.example.minicodevault.model.Learning;
import com.example.minicodevault.model.Order;
import com.example.minicodevault.model.Review;
import com.example.minicodevault.repository.InMemoryStore;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;

@Component
public class ViewMapper {
    private final InMemoryStore store;
    private final Clock clock;

    public ViewMapper(InMemoryStore store, Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    public Views.Course course(Learning course) {
        List<Review> reviews = store.reviewsFor(course.getId());
        return new Views.Course(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getType(),
                course.getAccessInfo(),
                course.getStatus(),
                course.isAvailable(),
                course.isArchived(),
                store.findUser(course.getInstructorId()).orElseThrow().name(),
                reviews.size(),
                reviews.stream().mapToInt(Review::rating).average().orElse(0));
    }

    public Views.Access access(ContentAccess access, long learnerId) {
        return new Views.Access(
                course(access.getLearning()),
                access.isActive(clock.instant()),
                access.getPurchasedAt(),
                access.getExpiresAt(),
                access.getRemainingDownloads(),
                store.findReview(learnerId, access.getLearning().getId()));
    }

    public Views.Purchase purchase(Order order) {
        return new Views.Purchase(
                order.getId(),
                store.findUser(order.getLearnerId()).orElseThrow().name(),
                order.getItems().stream().map(this::course).toList(),
                order.getPaymentCode(),
                order.getSubtotal(),
                order.getFee(),
                order.getTotal(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getPaidAt());
    }
}
