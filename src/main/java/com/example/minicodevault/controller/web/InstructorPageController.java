package com.example.minicodevault.controller.web;

import com.example.minicodevault.controller.SessionIdentity;
import com.example.minicodevault.model.User;
import com.example.minicodevault.service.Authorization;
import com.example.minicodevault.service.CatalogService;
import com.example.minicodevault.service.PurchaseService;
import com.example.minicodevault.service.ReportService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/instructor")
public class InstructorPageController {
    private final CatalogService catalog;
    private final PurchaseService purchases;
    private final ReportService reports;
    private final SessionIdentity identity;

    public InstructorPageController(
            CatalogService catalog,
            PurchaseService purchases,
            ReportService reports,
            SessionIdentity identity) {
        this.catalog = catalog;
        this.purchases = purchases;
        this.reports = reports;
        this.identity = identity;
    }

    private User instructor(HttpSession session) {
        User user = identity.current(session);
        Authorization.requireRole(user, User.Role.INSTRUCTOR);
        return user;
    }

    @GetMapping("/courses")
    public String courses(HttpSession session, Model model) {
        model.addAttribute("courses", catalog.instructorLearning(instructor(session)));
        return "instructor/courses";
    }

    @GetMapping("/courses/new")
    public String create(HttpSession session, Model model) {
        instructor(session);
        model.addAttribute("form", LearningForm.empty());
        model.addAttribute("courseId", null);
        return "instructor/form";
    }

    @GetMapping("/courses/{id}/edit")
    public String edit(@PathVariable long id, HttpSession session, Model model) {
        model.addAttribute(
                "form", LearningForm.from(catalog.editLearning(instructor(session), id)));
        model.addAttribute("courseId", id);
        return "instructor/form";
    }

    @PostMapping("/courses")
    public String create(
            @ModelAttribute("form") LearningForm form,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {
        return save(null, form, session, model, redirect);
    }

    @PostMapping("/courses/{id}")
    public String update(
            @PathVariable long id,
            @ModelAttribute("form") LearningForm form,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {
        return save(id, form, session, model, redirect);
    }

    private String save(
            Long id,
            LearningForm form,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {
        User user = instructor(session);
        try {
            catalog.saveLearning(user, id, form.toInput());
            redirect.addFlashAttribute("success", "Course berhasil disimpan.");
            return "redirect:/instructor/courses";
        } catch (IllegalArgumentException error) {
            model.addAttribute("courseId", id);
            model.addAttribute("error", error.getMessage());
            return "instructor/form";
        }
    }

    @PostMapping("/courses/{id}/archive")
    public String archive(@PathVariable long id, HttpSession session, RedirectAttributes redirect) {
        catalog.deleteLearning(instructor(session), id);
        redirect.addFlashAttribute(
                "success", "Course diarsipkan. Hak akses pembeli lama tetap berlaku.");
        return "redirect:/instructor/courses";
    }

    @GetMapping("/transactions")
    public String transactions(HttpSession session, Model model) {
        model.addAttribute("orders", purchases.orders(instructor(session)));
        model.addAttribute("seller", true);
        return "learner/history";
    }

    @GetMapping("/sales")
    public String sales(HttpSession session, Model model) {
        model.addAttribute("report", reports.report(instructor(session)));
        return "instructor/sales";
    }

    @GetMapping("/reviews")
    public String reviews(HttpSession session, Model model) {
        model.addAttribute("reviews", reports.report(instructor(session)).reviews());
        return "instructor/reviews";
    }
}
