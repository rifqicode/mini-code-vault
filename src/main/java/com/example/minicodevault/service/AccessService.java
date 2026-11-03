package com.example.minicodevault.service;

import com.example.minicodevault.dto.Views;
import com.example.minicodevault.model.CodingTest;
import com.example.minicodevault.model.ContentAccess;
import com.example.minicodevault.model.EBook;
import com.example.minicodevault.model.Review;
import com.example.minicodevault.model.User;
import com.example.minicodevault.model.VideoCourse;
import com.example.minicodevault.repository.InMemoryStore;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Service
public class AccessService {
    private final InMemoryStore store;
    private final ViewMapper mapper;
    private final Clock clock;

    public AccessService(InMemoryStore store, ViewMapper mapper, Clock clock) {
        this.store = store;
        this.mapper = mapper;
        this.clock = clock;
    }

    public List<Views.Access> library(User user) {
        Authorization.requireRole(user, User.Role.LEARNER);
        return store.transaction(() ->
                store.allAccess(user.id()).stream()
                        .map(access -> mapper.access(access, user.id()))
                        .toList());
    }

    public Views.Lesson lesson(User user, long id) {
        return store.transaction(() -> {
            ContentAccess access = Authorization.access(store, user, id);
            if (!access.isActive(clock.instant()))
                throw new AppException(403, "Masa akses berakhir atau jatah unduh habis.");
            return new Views.Lesson(
                    mapper.access(access, user.id()),
                    access.getLearning() instanceof VideoCourse video
                            ? video.getChapters()
                            : List.of(),
                    access.getLearning() instanceof CodingTest coding
                            ? coding.getQuestions()
                            : List.of());
        });
    }

    public String download(User user, long id) {
        return store.transaction(() -> {
            ContentAccess access = Authorization.access(store, user, id);
            access.consumeDownload(clock.instant());
            EBook ebook = (EBook) access.getLearning();
            return ebook.getTitle() + "\n\n" + ebook.getText();
        });
    }

    public void checkVideo(User user, long id) {
        store.transaction(() -> {
            ContentAccess access = Authorization.access(store, user, id);
            if (!(access.getLearning() instanceof VideoCourse))
                throw new IllegalArgumentException("Course ini bukan video.");
            if (!access.isActive(clock.instant()))
                throw new AppException(403, "Masa akses video telah berakhir.");
            return null;
        });
    }

    public void review(User user, long id, int rating, String comment) {
        store.transaction(() -> {
            Authorization.access(store, user, id);
            store.saveReview(
                    new Review(id, user.id(), user.name(), rating, comment, clock.instant()));
            return null;
        });
    }
}
