package com.example.minicodevault.model;

import java.math.BigDecimal;

public final class EBook extends Learning {
    private final int maxDownloads;
    private final String text;

    public EBook(
            long id,
            long owner,
            String title,
            String description,
            BigDecimal price,
            Status status,
            boolean available,
            int maxDownloads,
            String text) {
        super(id, owner, title, description, price, status, available);
        if (maxDownloads < 1 || maxDownloads > 100)
            throw new IllegalArgumentException("Batas unduh harus 1 sampai 100.");
        if (text == null || text.isBlank() || text.length() > 50000)
            throw new IllegalArgumentException("Isi e-book wajib diisi, maksimal 50.000 karakter.");
        this.maxDownloads = maxDownloads;
        this.text = text;
    }

    public int getMaxDownloads() {
        return maxDownloads;
    }

    public String getText() {
        return text;
    }

    @Override
    public String getType() {
        return "EBOOK";
    }

    @Override
    public String getAccessInfo() {
        return "File TXT • Maksimal " + maxDownloads + " kali unduh";
    }

    @Override
    public Learning snapshot() {
        return new EBook(
                getId(),
                getInstructorId(),
                getTitle(),
                getDescription(),
                getPrice(),
                getStatus(),
                isAvailable(),
                maxDownloads,
                text);
    }
}
