package com.example.minicodevault.model;

import java.math.BigDecimal;

public abstract class Learning {
    public enum Status {
        DRAFT,
        PUBLISHED,
        SUSPENDED
    }

    private final long id;
    private final long instructorId;
    private final String title;
    private final String description;
    private final BigDecimal price;
    private final Status status;
    private final boolean available;
    private boolean archived;

    protected Learning(
            long id,
            long instructorId,
            String title,
            String description,
            BigDecimal price,
            Status status,
            boolean available) {
        this.id = id;
        this.instructorId = instructorId;
        if (title == null || title.isBlank() || title.length() > 160)
            throw new IllegalArgumentException("Judul wajib diisi, maksimal 160 karakter.");
        if (description == null || description.isBlank() || description.length() > 4000)
            throw new IllegalArgumentException("Deskripsi wajib diisi, maksimal 4000 karakter.");
        if (price == null
                || price.signum() < 0
                || price.stripTrailingZeros().scale() > 0
                || price.compareTo(new BigDecimal("100000000")) > 0)
            throw new IllegalArgumentException(
                    "Harga harus rupiah bulat antara 0 dan 100.000.000.");
        this.title = title.trim();
        this.description = description.trim();
        this.price = price.setScale(0);
        if (status == null) throw new IllegalArgumentException("Status wajib dipilih.");
        this.status = status;
        this.available = available;
    }

    public final void archive() {
        archived = true;
    }

    public final boolean isPurchasable() {
        return !archived && available && status == Status.PUBLISHED;
    }

    public final long getId() {
        return id;
    }

    public final long getInstructorId() {
        return instructorId;
    }

    public final String getTitle() {
        return title;
    }

    public final String getDescription() {
        return description;
    }

    public final BigDecimal getPrice() {
        return price;
    }

    public final Status getStatus() {
        return status;
    }

    public final boolean isAvailable() {
        return available && !archived;
    }

    public final boolean isArchived() {
        return archived;
    }

    public abstract String getType();

    public abstract String getAccessInfo();

    public abstract Learning snapshot();
}
