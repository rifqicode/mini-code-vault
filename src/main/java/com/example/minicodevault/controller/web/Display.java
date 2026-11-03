package com.example.minicodevault.controller.web;

import com.example.minicodevault.dto.Views;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component("display")
public class Display {
    private static final Locale INDONESIAN = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", INDONESIAN)
                    .withZone(ZoneId.of("Asia/Jakarta"));

    public String money(BigDecimal amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(INDONESIAN);
        format.setMaximumFractionDigits(0);
        return format.format(amount);
    }

    public String date(Instant instant) {
        return instant == null ? "-" : DATE.format(instant);
    }

    public String type(String type) {
        return switch (type) {
            case "EBOOK" -> "Modul E-Book";
            case "VIDEO" -> "Video Course";
            case "CODING" -> "Coding Test";
            default -> type;
        };
    }

    public String status(String status) {
        return switch (status) {
            case "DRAFT" -> "Draft";
            case "PUBLISHED" -> "Dipublikasikan";
            case "SUSPENDED" -> "Ditangguhkan";
            case "PAID" -> "Lunas";
            case "PENDING" -> "Menunggu pembayaran";
            case "CANCELLED" -> "Dibatalkan";
            default -> status;
        };
    }

    public String payment(String code) {
        return switch (code) {
            case "BANK" -> "Transfer bank";
            case "CARD" -> "Kartu kredit/debit";
            case "EWALLET" -> "E-wallet";
            default -> code;
        };
    }

    public String access(Views.Access access) {
        if (access == null) return "";
        return switch (access.course().type()) {
            case "EBOOK" -> "Sisa " + access.remainingDownloads() + " unduhan";
            case "VIDEO" ->
                    (access.active() ? "Aktif hingga " : "Masa akses berakhir pada ")
                            + date(access.expiresAt());
            default -> "Akses soal dan pembahasan tanpa batas";
        };
    }
}
