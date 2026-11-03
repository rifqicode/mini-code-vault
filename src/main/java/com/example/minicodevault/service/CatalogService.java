package com.example.minicodevault.service;

import com.example.minicodevault.dto.Views;
import com.example.minicodevault.model.CodingTest;
import com.example.minicodevault.model.EBook;
import com.example.minicodevault.model.Learning;
import com.example.minicodevault.model.User;
import com.example.minicodevault.model.VideoCourse;
import com.example.minicodevault.repository.InMemoryStore;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {
    private final InMemoryStore store;
    private final ViewMapper mapper;
    private final LearningFactory factory;

    public CatalogService(InMemoryStore store, ViewMapper mapper, LearningFactory factory) {
        this.store = store;
        this.mapper = mapper;
        this.factory = factory;
    }

    public List<Views.Course> catalog() {
        return store.transaction(() ->
                store.allLearning().stream()
                        .filter(course ->
                                !course.isArchived()
                                        && course.getStatus() == Learning.Status.PUBLISHED)
                        .map(mapper::course)
                        .toList());
    }

    public Views.CourseDetail detail(long id) {
        return store.transaction(() -> {
            Learning course = Authorization.learning(store, id);
            if (course.isArchived() || course.getStatus() != Learning.Status.PUBLISHED) {
                throw new AppException(404, "Course tidak tersedia di katalog.");
            }
            return new Views.CourseDetail(
                    mapper.course(course),
                    store.reviewsFor(id),
                    course instanceof VideoCourse video ? video.getChapters() : List.of(),
                    course instanceof CodingTest coding
                            ? coding.getQuestions().stream()
                                    .map(q -> q.difficulty().name())
                                    .toList()
                            : List.of());
        });
    }

    public List<Views.Course> instructorLearning(User user) {
        Authorization.requireRole(user, User.Role.INSTRUCTOR);
        return store.transaction(() ->
                store.allLearning().stream()
                        .filter(course -> course.getInstructorId() == user.id())
                        .map(mapper::course)
                        .toList());
    }

    public Views.EditableCourse editLearning(User user, long id) {
        return store.transaction(() -> {
            Learning course = Authorization.ownLearning(store, user, id);
            return new Views.EditableCourse(
                    mapper.course(course),
                    course instanceof EBook ebook ? ebook.getMaxDownloads() : null,
                    course instanceof EBook ebook ? ebook.getText() : null,
                    course instanceof VideoCourse video ? video.getAccessDays() : null,
                    course instanceof VideoCourse video ? video.getDurationMinutes() : null,
                    course instanceof VideoCourse video ? video.getChapters() : List.of(),
                    course instanceof CodingTest coding ? coding.getQuestions() : List.of());
        });
    }

    public long saveLearning(User user, Long id, LearningInput input) {
        Authorization.requireRole(user, User.Role.INSTRUCTOR);
        return store.transaction(() -> {
            if (id != null) {
                Learning old = Authorization.ownLearning(store, user, id);
                if (old.isArchived())
                    throw new IllegalArgumentException("Course arsip tidak dapat diedit.");
                if (!old.getType().equals(input.type()))
                    throw new IllegalArgumentException(
                            "Jenis course tidak dapat diubah; buat course baru.");
            }
            long actualId = id == null ? store.nextLearningId() : id;
            store.saveLearning(factory.create(user, actualId, input));
            return actualId;
        });
    }

    public void deleteLearning(User user, long id) {
        store.transaction(() -> {
            Authorization.ownLearning(store, user, id).archive();
            return null;
        });
    }
}
