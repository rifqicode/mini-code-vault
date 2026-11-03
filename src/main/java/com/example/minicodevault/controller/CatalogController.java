package com.example.minicodevault.controller;

import com.example.minicodevault.dto.Views;
import com.example.minicodevault.service.CatalogService;
import com.example.minicodevault.service.LearningInput;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService catalog;
    private final SessionIdentity identity;

    public CatalogController(CatalogService catalog, SessionIdentity identity) {
        this.catalog = catalog;
        this.identity = identity;
    }

    @GetMapping("/learning")
    public List<Views.Course> catalog() {
        return catalog.catalog();
    }

    @GetMapping("/learning/{id}")
    public Views.CourseDetail detail(@PathVariable long id) {
        return catalog.detail(id);
    }

    @GetMapping("/instructor/learning")
    public List<Views.Course> instructorLearning(HttpSession session) {
        return catalog.instructorLearning(identity.current(session));
    }

    @GetMapping("/instructor/learning/{id}")
    public Views.EditableCourse editLearning(@PathVariable long id, HttpSession session) {
        return catalog.editLearning(identity.current(session), id);
    }

    @PostMapping("/instructor/learning")
    public Views.Created createLearning(@RequestBody LearningInput input, HttpSession session) {
        return new Views.Created(catalog.saveLearning(identity.current(session), null, input));
    }

    @PutMapping("/instructor/learning/{id}")
    public Views.Created updateLearning(
            @PathVariable long id, @RequestBody LearningInput input, HttpSession session) {
        return new Views.Created(catalog.saveLearning(identity.current(session), id, input));
    }

    @DeleteMapping("/instructor/learning/{id}")
    public Views.Result deleteLearning(@PathVariable long id, HttpSession session) {
        catalog.deleteLearning(identity.current(session), id);
        return Views.Result.success();
    }
}
