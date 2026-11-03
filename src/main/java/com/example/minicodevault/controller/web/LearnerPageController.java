package com.example.minicodevault.controller.web;

import com.example.minicodevault.controller.SessionIdentity;
import com.example.minicodevault.dto.Views;
import com.example.minicodevault.model.User;
import com.example.minicodevault.service.AccessService;
import com.example.minicodevault.service.AppException;
import com.example.minicodevault.service.Authorization;
import com.example.minicodevault.service.PurchaseService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;

@Controller
public class LearnerPageController {
    private final PurchaseService purchases;
    private final AccessService access;
    private final SessionIdentity identity;

    public LearnerPageController(
            PurchaseService purchases, AccessService access, SessionIdentity identity) {
        this.purchases = purchases;
        this.access = access;
        this.identity = identity;
    }

    @GetMapping("/cart")
    public String cartPage(HttpSession session, Model model) {
        model.addAttribute("cart", purchases.cart(identity.current(session)));
        return "learner/cart";
    }

    @GetMapping("/cart/panel")
    public String cartPanel(HttpSession session, Model model) {
        model.addAttribute("cart", purchases.cart(identity.current(session)));
        return "fragments/cart :: contents";
    }

    @PostMapping("/cart/items/{id}")
    public String addCart(@PathVariable long id, HttpSession session, RedirectAttributes redirect) {
        try {
            purchases.addCart(identity.current(session), id);
            redirect.addFlashAttribute("success", "Course ditambahkan ke keranjang.");
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", error.getMessage());
        }
        return "redirect:/catalog?cart=open";
    }

    @PostMapping("/cart/items/{id}/remove")
    public String removeCart(
            @PathVariable long id, HttpSession session, RedirectAttributes redirect) {
        purchases.removeCart(identity.current(session), id);
        return "redirect:/catalog?cart=open";
    }

    @PostMapping("/checkout")
    public String checkout(
            @RequestParam String method, HttpSession session, RedirectAttributes redirect) {
        try {
            Views.Purchase order = purchases.checkout(identity.current(session), method);
            redirect.addFlashAttribute(
                    "success",
                    "Pesanan #"
                            + order.id()
                            + " langsung diterima. Course sudah tersedia di koleksi.");
            return "redirect:/history";
        } catch (IllegalArgumentException error) {
            redirect.addFlashAttribute("error", error.getMessage());
            return "redirect:/catalog?cart=open";
        }
    }

    @GetMapping("/library")
    public String library(HttpSession session, Model model) {
        model.addAttribute("library", access.library(identity.current(session)));
        return "learner/library";
    }

    @GetMapping("/library/{id}")
    public String lesson(@PathVariable long id, HttpSession session, Model model) {
        model.addAttribute("lesson", access.lesson(identity.current(session), id));
        return "learner/lesson";
    }

    @PostMapping("/library/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable long id, HttpSession session) {
        byte[] bytes =
                access.download(identity.current(session), id).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("codevault-ebook-" + id + ".txt")
                                .build()
                                .toString())
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .cacheControl(CacheControl.noStore())
                .body(bytes);
    }

    @GetMapping("/history")
    public String history(HttpSession session, Model model) {
        User user = identity.current(session);
        Authorization.requireRole(user, User.Role.LEARNER);
        model.addAttribute("orders", purchases.orders(user));
        model.addAttribute("seller", false);
        return "learner/history";
    }

    @GetMapping("/library/{id}/review")
    public String reviewPage(@PathVariable long id, HttpSession session, Model model) {
        Views.Access owned =
                access.library(identity.current(session)).stream()
                        .filter(item -> item.course().id() == id)
                        .findFirst()
                        .orElseThrow(() ->
                                new AppException(
                                        403,
                                        "Ulasan hanya dapat diberikan untuk course yang sudah"
                                            + " dibeli."));
        model.addAttribute("owned", owned);
        model.addAttribute("rating", owned.myReview() == null ? 5 : owned.myReview().rating());
        model.addAttribute("comment", owned.myReview() == null ? "" : owned.myReview().comment());
        return "learner/review";
    }

    @PostMapping("/library/{id}/review")
    public String review(
            @PathVariable long id,
            @RequestParam int rating,
            @RequestParam String comment,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {
        try {
            access.review(identity.current(session), id, rating, comment);
            redirect.addFlashAttribute("success", "Ulasan berhasil disimpan.");
            return "redirect:/library";
        } catch (IllegalArgumentException error) {
            reviewPage(id, session, model);
            model.addAttribute("rating", rating);
            model.addAttribute("comment", comment);
            model.addAttribute("error", error.getMessage());
            return "learner/review";
        }
    }
}
