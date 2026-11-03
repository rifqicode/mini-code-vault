package com.example.minicodevault.controller.web;

import com.example.minicodevault.controller.SessionIdentity;
import com.example.minicodevault.service.CatalogService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Locale;

@Controller
public class CatalogPageController {
    private final CatalogService catalog;
    private final SessionIdentity identity;

    public CatalogPageController(CatalogService catalog, SessionIdentity identity) {
        this.catalog = catalog;
        this.identity = identity;
    }

    @GetMapping("/catalog")
    public String catalog(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String type,
            HttpSession session,
            Model model) {
        identity.current(session);
        String search = q.strip().toLowerCase(Locale.ROOT);
        model.addAttribute(
                "courses",
                catalog.catalog().stream()
                        .filter(course -> type.isBlank() || course.type().equals(type))
                        .filter(course ->
                                (course.title() + " " + course.description())
                                        .toLowerCase(Locale.ROOT)
                                        .contains(search))
                        .toList());
        model.addAttribute("q", q);
        model.addAttribute("type", type);
        return "catalog/index";
    }

    @GetMapping("/courses/{id}")
    public String detail(@PathVariable long id, HttpSession session, Model model) {
        identity.current(session);
        model.addAttribute("detail", catalog.detail(id));
        return "catalog/detail";
    }
}
