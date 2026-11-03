package com.example.minicodevault.dto;

import com.example.minicodevault.model.CodingTest;
import com.example.minicodevault.model.Learning;
import com.example.minicodevault.model.Order;
import com.example.minicodevault.model.Review;
import com.example.minicodevault.model.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class Views {
    private Views() {}

    public record SessionView(String csrf, User user) {}

    public record Result(boolean ok) {
        public static Result success() {
            return new Result(true);
        }
    }

    public record Created(long id) {}

    public record ErrorView(String error) {}

    public record Course(
            long id,
            String title,
            String description,
            BigDecimal price,
            String type,
            String accessInfo,
            Learning.Status status,
            boolean available,
            boolean archived,
            String instructor,
            int reviewCount,
            double rating) {}

    public record CourseDetail(
            Course course,
            List<Review> reviews,
            List<String> chapters,
            List<String> difficulties) {}

    public record EditableCourse(
            Course course,
            Integer maxDownloads,
            String ebookText,
            Integer accessDays,
            Integer durationMinutes,
            List<String> chapters,
            List<CodingTest.Question> questions) {}

    public record Access(
            Course course,
            boolean active,
            Instant purchasedAt,
            Instant expiresAt,
            int remainingDownloads,
            Review myReview) {}

    public record Lesson(
            Access access, List<String> chapters, List<CodingTest.Question> questions) {}

    public record PaymentOption(String code, String label, boolean manual) {}

    public record Quote(BigDecimal fee, BigDecimal total) {}

    public record Cart(List<Course> items, BigDecimal subtotal, Map<String, Quote> quotes) {}

    public record Purchase(
            long id,
            String learner,
            List<Course> items,
            String paymentCode,
            BigDecimal subtotal,
            BigDecimal fee,
            BigDecimal total,
            Order.Status status,
            Instant createdAt,
            Instant paidAt) {}

    public record CourseSales(long id, String title, long count, BigDecimal revenue) {}

    public record CourseReview(String title, Review review) {}

    public record Report(
            long transactionCount,
            BigDecimal revenue,
            BigDecimal fees,
            List<CourseSales> byLearning,
            Map<String, Long> byPayment,
            List<CourseReview> reviews) {}
}
