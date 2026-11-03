package com.example.minicodevault.model;

import java.math.BigDecimal;
import java.util.List;

public final class VideoCourse extends Learning {
    private final int accessDays;
    private final int durationMinutes;
    private final List<String> chapters;

    public VideoCourse(
            long id,
            long owner,
            String title,
            String description,
            BigDecimal price,
            Status status,
            boolean available,
            int accessDays,
            int durationMinutes,
            List<String> chapters) {
        super(id, owner, title, description, price, status, available);
        if (accessDays < 1 || accessDays > 3650)
            throw new IllegalArgumentException("Masa akses harus 1 sampai 3650 hari.");
        if (durationMinutes < 1 || durationMinutes > 100000)
            throw new IllegalArgumentException("Durasi harus 1 sampai 100.000 menit.");
        if (chapters == null
                || chapters.isEmpty()
                || chapters.size() > 100
                || chapters.stream().anyMatch(s -> s == null || s.isBlank() || s.length() > 300))
            throw new IllegalArgumentException(
                    "Isi 1 sampai 100 judul chapter, maksimal 300 karakter per judul.");
        this.accessDays = accessDays;
        this.durationMinutes = durationMinutes;
        this.chapters = List.copyOf(chapters);
    }

    public int getAccessDays() {
        return accessDays;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public List<String> getChapters() {
        return chapters;
    }

    @Override
    public String getType() {
        return "VIDEO";
    }

    @Override
    public String getAccessInfo() {
        return durationMinutes
                + " menit • "
                + chapters.size()
                + " chapter • Akses "
                + accessDays
                + " hari";
    }

    @Override
    public Learning snapshot() {
        return new VideoCourse(
                getId(),
                getInstructorId(),
                getTitle(),
                getDescription(),
                getPrice(),
                getStatus(),
                isAvailable(),
                accessDays,
                durationMinutes,
                chapters);
    }
}
