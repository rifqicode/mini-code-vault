package com.example.minicodevault.model;

import java.time.Instant;

public record Review(
        long learningId,
        long learnerId,
        String learnerName,
        int rating,
        String comment,
        Instant updatedAt) {
    public Review {
        if (rating < 1 || rating > 5)
            throw new IllegalArgumentException("Rating harus 1 sampai 5.");
        if (comment == null || comment.isBlank() || comment.length() > 2000)
            throw new IllegalArgumentException("Ulasan wajib diisi, maksimal 2000 karakter.");
        comment = comment.trim();
    }
}
