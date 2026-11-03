package com.example.minicodevault.service;

import com.example.minicodevault.dto.Views;
import com.example.minicodevault.model.Learning;
import com.example.minicodevault.model.Order;
import com.example.minicodevault.model.User;
import com.example.minicodevault.repository.InMemoryStore;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    private final InMemoryStore store;
    private final PaymentRegistry payments;

    public ReportService(InMemoryStore store, PaymentRegistry payments) {
        this.store = store;
        this.payments = payments;
    }

    public Views.Report report(User user) {
        Authorization.requireRole(user, User.Role.INSTRUCTOR);
        return store.transaction(() -> {
            List<Order> paid =
                    store.allOrders().stream()
                            .filter(order ->
                                    order.getStatus() == Order.Status.PAID
                                            && order.getItems().stream()
                                                    .allMatch(course ->
                                                            course.getInstructorId() == user.id()))
                            .toList();
            List<Views.CourseSales> byLearning =
                    store.allLearning().stream()
                            .filter(course -> course.getInstructorId() == user.id())
                            .map(course -> sales(course, paid))
                            .toList();
            Map<String, Long> byPayment = new LinkedHashMap<>();
            payments.options()
                    .forEach(payment ->
                            byPayment.put(
                                    payment.code(),
                                    paid.stream()
                                            .filter(order ->
                                                    order.getPaymentCode().equals(payment.code()))
                                            .count()));
            List<Views.CourseReview> reviews =
                    store.allReviews().stream()
                            .filter(review ->
                                    Authorization.learning(store, review.learningId())
                                                    .getInstructorId()
                                            == user.id())
                            .map(review ->
                                    new Views.CourseReview(
                                            Authorization.learning(store, review.learningId())
                                                    .getTitle(),
                                            review))
                            .toList();
            return new Views.Report(
                    paid.size(),
                    paid.stream().map(Order::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add),
                    paid.stream().map(Order::getFee).reduce(BigDecimal.ZERO, BigDecimal::add),
                    byLearning,
                    Map.copyOf(byPayment),
                    reviews);
        });
    }

    private Views.CourseSales sales(Learning course, List<Order> paid) {
        List<Learning> sold =
                paid.stream()
                        .flatMap(order -> order.getItems().stream())
                        .filter(item -> item.getId() == course.getId())
                        .toList();
        return new Views.CourseSales(
                course.getId(),
                course.getTitle(),
                sold.size(),
                sold.stream().map(Learning::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
