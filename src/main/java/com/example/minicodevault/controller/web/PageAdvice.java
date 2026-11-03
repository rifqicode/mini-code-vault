package com.example.minicodevault.controller.web;

import com.example.minicodevault.controller.SessionIdentity;
import com.example.minicodevault.dto.Views;
import com.example.minicodevault.model.User;
import com.example.minicodevault.service.AccessService;
import com.example.minicodevault.service.AppException;
import com.example.minicodevault.service.PurchaseService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice(basePackages = "com.example.minicodevault.controller.web")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PageAdvice {
    private final SessionIdentity identity;
    private final PurchaseService purchases;
    private final AccessService access;

    public PageAdvice(SessionIdentity identity, PurchaseService purchases, AccessService access) {
        this.identity = identity;
        this.purchases = purchases;
        this.access = access;
    }

    @ModelAttribute
    public void shared(Model model, HttpSession session, HttpServletRequest request) {
        User user = identity.optional(session);
        model.addAttribute("currentUser", user);
        model.addAttribute("csrf", identity.csrf(session));
        model.addAttribute("currentPath", request.getRequestURI());
        boolean learner = user != null && user.role() == User.Role.LEARNER;
        model.addAttribute("isLearner", learner);
        model.addAttribute("isInstructor", user != null && user.role() == User.Role.INSTRUCTOR);
        if (learner) {
            Views.Cart cart = purchases.cart(user);
            List<Views.Access> library = access.library(user);
            model.addAttribute("cartCount", cart.items().size());
            model.addAttribute("cartIds", cart.items().stream().map(Views.Course::id).toList());
            model.addAttribute(
                    "ownedIds", library.stream().map(item -> item.course().id()).toList());
            model.addAttribute(
                    "accessById",
                    library.stream()
                            .collect(Collectors.toMap(item -> item.course().id(), item -> item)));
        } else {
            model.addAttribute("cartCount", 0);
            model.addAttribute("cartIds", List.of());
            model.addAttribute("ownedIds", List.of());
            model.addAttribute("accessById", Map.of());
        }
    }

    @ExceptionHandler(AppException.class)
    public ModelAndView application(AppException error, Model model, HttpServletResponse response) {
        if (error.getStatus() == 401) {
            RedirectView redirect = new RedirectView("/login");
            redirect.setExposeModelAttributes(false);
            return new ModelAndView(redirect);
        }
        response.setStatus(error.getStatus());
        model.addAttribute("error", error.getMessage());
        return new ModelAndView("error", model.asMap());
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        BindException.class,
        MethodArgumentTypeMismatchException.class
    })
    public String validation(Exception error, Model model, HttpServletResponse response) {
        response.setStatus(400);
        model.addAttribute(
                "error",
                error instanceof IllegalArgumentException
                        ? error.getMessage()
                        : "Format formulir tidak valid. Periksa kembali data yang diisi.");
        return "error";
    }
}
