package com.example.minicodevault.controller.web;

import com.example.minicodevault.controller.SessionIdentity;
import com.example.minicodevault.model.User;
import com.example.minicodevault.service.AppException;
import com.example.minicodevault.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthPageController {
    private final AuthService auth;
    private final SessionIdentity identity;

    public AuthPageController(AuthService auth, SessionIdentity identity) {
        this.auth = auth;
        this.identity = identity;
    }

    @GetMapping("/")
    public String home(HttpSession session, RedirectAttributes redirect) {
        User user = identity.optional(session);
        return user == null ? "redirect:/login" : destination(user);
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(defaultValue = "false") boolean expired, Model model) {
        if (expired) model.addAttribute("error", "Session berubah. Silakan masuk kembali.");
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            RedirectAttributes redirect) {
        try {
            User user = auth.login(username, password);
            request.changeSessionId();
            request.getSession().setAttribute(SessionIdentity.USER_ID, user.id());
            return destination(user);
        } catch (AppException error) {
            redirect.addFlashAttribute("error", error.getMessage());
            redirect.addFlashAttribute("selectedUsername", username);
            return "redirect:/login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirect) {
        session.invalidate();
        return "redirect:/login";
    }

    private String destination(User user) {
        return user.role() == User.Role.INSTRUCTOR
                ? "redirect:/instructor/courses"
                : "redirect:/catalog";
    }
}
