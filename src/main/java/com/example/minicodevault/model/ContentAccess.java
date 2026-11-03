package com.example.minicodevault.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class ContentAccess {
    private final Learning learning;
    private final Instant purchasedAt;
    private int downloads;

    public ContentAccess(Learning learning, Instant purchasedAt) {
        this.learning = learning.snapshot();
        this.purchasedAt = purchasedAt;
    }

    public Instant getExpiresAt() {
        return learning instanceof VideoCourse video
                ? purchasedAt.plus(video.getAccessDays(), ChronoUnit.DAYS)
                : null;
    }

    public boolean isActive(Instant now) {
        if (learning instanceof EBook ebook) return downloads < ebook.getMaxDownloads();
        return getExpiresAt() == null || now.isBefore(getExpiresAt());
    }

    public void consumeDownload(Instant now) {
        if (!(learning instanceof EBook))
            throw new IllegalArgumentException("Course ini bukan e-book.");
        if (!isActive(now)) throw new IllegalArgumentException("Jatah unduhan sudah habis.");
        downloads++;
    }

    public Learning getLearning() {
        return learning;
    }

    public Instant getPurchasedAt() {
        return purchasedAt;
    }

    public int getRemainingDownloads() {
        return learning instanceof EBook ebook ? ebook.getMaxDownloads() - downloads : 0;
    }
}
