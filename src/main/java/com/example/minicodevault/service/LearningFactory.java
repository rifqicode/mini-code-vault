package com.example.minicodevault.service;

import com.example.minicodevault.model.CodingTest;
import com.example.minicodevault.model.EBook;
import com.example.minicodevault.model.Learning;
import com.example.minicodevault.model.User;
import com.example.minicodevault.model.VideoCourse;

import org.springframework.stereotype.Component;

@Component
public class LearningFactory {
    public Learning create(User instructor, long id, LearningInput input) {
        Learning.Status status;
        try {
            status = Learning.Status.valueOf(input.status());
        } catch (IllegalArgumentException | NullPointerException error) {
            throw new IllegalArgumentException("Pilih status course yang valid.");
        }
        if (input.type() == null) throw new IllegalArgumentException("Jenis course wajib dipilih.");
        return switch (input.type()) {
            case "EBOOK" ->
                    new EBook(
                            id,
                            instructor.id(),
                            input.title(),
                            input.description(),
                            input.price(),
                            status,
                            input.available(),
                            number(input.maxDownloads()),
                            input.ebookText());
            case "VIDEO" ->
                    new VideoCourse(
                            id,
                            instructor.id(),
                            input.title(),
                            input.description(),
                            input.price(),
                            status,
                            input.available(),
                            number(input.accessDays()),
                            number(input.durationMinutes()),
                            input.chapters());
            case "CODING" ->
                    new CodingTest(
                            id,
                            instructor.id(),
                            input.title(),
                            input.description(),
                            input.price(),
                            status,
                            input.available(),
                            input.questions());
            default -> throw new IllegalArgumentException("Jenis course tidak valid.");
        };
    }

    private int number(Integer value) {
        return value == null ? 0 : value;
    }
}
