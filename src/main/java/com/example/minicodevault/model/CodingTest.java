package com.example.minicodevault.model;

import java.math.BigDecimal;
import java.util.List;

public final class CodingTest extends Learning {
    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    public record Question(String title, Difficulty difficulty, String prompt, String solution) {
        public Question {
            if (title == null
                    || title.isBlank()
                    || difficulty == null
                    || prompt == null
                    || prompt.isBlank()
                    || solution == null
                    || solution.isBlank())
                throw new IllegalArgumentException(
                        "Setiap soal memerlukan judul, tingkat kesulitan, pertanyaan, dan"
                                + " pembahasan.");
            if (title.length() > 300 || prompt.length() > 5000 || solution.length() > 10000)
                throw new IllegalArgumentException("Teks soal melebihi batas panjang.");
        }
    }

    private final List<Question> questions;

    public CodingTest(
            long id,
            long owner,
            String title,
            String description,
            BigDecimal price,
            Status status,
            boolean available,
            List<Question> questions) {
        super(id, owner, title, description, price, status, available);
        if (questions == null
                || questions.isEmpty()
                || questions.size() > 100
                || questions.stream().anyMatch(java.util.Objects::isNull))
            throw new IllegalArgumentException("Paket wajib berisi 1 sampai 100 soal.");
        this.questions = List.copyOf(questions);
    }

    public List<Question> getQuestions() {
        return questions;
    }

    @Override
    public String getType() {
        return "CODING";
    }

    @Override
    public String getAccessInfo() {
        return questions.size() + " soal & pembahasan • Akses berulang tanpa batas";
    }

    @Override
    public Learning snapshot() {
        return new CodingTest(
                getId(),
                getInstructorId(),
                getTitle(),
                getDescription(),
                getPrice(),
                getStatus(),
                isAvailable(),
                questions);
    }
}
