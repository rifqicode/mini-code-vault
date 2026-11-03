package com.example.minicodevault.controller.web;

import com.example.minicodevault.dto.Views;
import com.example.minicodevault.model.CodingTest;
import com.example.minicodevault.service.LearningInput;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record LearningForm(
        String type,
        String title,
        String description,
        String price,
        String status,
        Boolean available,
        String maxDownloads,
        String ebookText,
        String accessDays,
        String durationMinutes,
        String chapters,
        String questions) {
    public static LearningForm empty() {
        return new LearningForm("EBOOK", "", "", "", "DRAFT", true, "3", "", "90", "120", "", "");
    }

    public static LearningForm from(Views.EditableCourse editable) {
        return new LearningForm(
                editable.course().type(),
                editable.course().title(),
                editable.course().description(),
                editable.course().price().toPlainString(),
                editable.course().status().name(),
                editable.course().available(),
                text(editable.maxDownloads()),
                editable.ebookText(),
                text(editable.accessDays()),
                text(editable.durationMinutes()),
                String.join("\n", editable.chapters()),
                editable.questions().stream()
                        .map(question ->
                                String.join(
                                        " | ",
                                        question.title(),
                                        question.difficulty().name(),
                                        question.prompt(),
                                        question.solution()))
                        .collect(Collectors.joining("\n")));
    }

    public LearningInput toInput() {
        BigDecimal amount;
        try {
            amount = new BigDecimal(price == null ? "" : price);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("Harga wajib berupa angka rupiah.");
        }
        return new LearningInput(
                type,
                title,
                description,
                amount,
                status,
                Boolean.TRUE.equals(available),
                "EBOOK".equals(type) ? number(maxDownloads) : null,
                ebookText,
                "VIDEO".equals(type) ? number(accessDays) : null,
                "VIDEO".equals(type) ? number(durationMinutes) : null,
                "VIDEO".equals(type) ? lines(chapters) : List.of(),
                "CODING".equals(type) ? parseQuestions() : List.of());
    }

    private List<CodingTest.Question> parseQuestions() {
        return lines(questions).stream()
                .map(line -> {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length != 4)
                        throw new IllegalArgumentException(
                                "Setiap soal harus memiliki empat bagian yang dipisahkan karakter"
                                    + " |.");
                    try {
                        return new CodingTest.Question(
                                parts[0].strip(),
                                CodingTest.Difficulty.valueOf(parts[1].strip()),
                                parts[2].strip(),
                                parts[3].strip());
                    } catch (IllegalArgumentException error) {
                        throw new IllegalArgumentException(
                                "Lengkapi judul, tingkat EASY/MEDIUM/HARD, pertanyaan, dan"
                                    + " pembahasan pada setiap soal.");
                    }
                })
                .toList();
    }

    private static List<String> lines(String value) {
        return value == null
                ? List.of()
                : Arrays.stream(value.split("\\R"))
                        .map(String::strip)
                        .filter(line -> !line.isEmpty())
                        .toList();
    }

    private static Integer number(String value) {
        try {
            return Integer.valueOf(value == null ? "" : value);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(
                    "Batas unduhan, masa akses, dan durasi wajib berupa angka bulat.");
        }
    }

    private static String text(Integer value) {
        return value == null ? "" : value.toString();
    }
}
