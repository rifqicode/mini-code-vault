package com.example.minicodevault.controller;

import com.example.minicodevault.dto.Views;
import com.example.minicodevault.service.PaymentRegistry;
import com.example.minicodevault.service.PurchaseService;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PurchaseController {
    public record CheckoutInput(String method) {}

    private final PurchaseService purchases;
    private final PaymentRegistry payments;
    private final SessionIdentity identity;

    public PurchaseController(
            PurchaseService purchases, PaymentRegistry payments, SessionIdentity identity) {
        this.purchases = purchases;
        this.payments = payments;
        this.identity = identity;
    }

    @GetMapping("/payments")
    public List<Views.PaymentOption> payments() {
        return payments.options();
    }

    @GetMapping("/cart")
    public Views.Cart cart(HttpSession session) {
        return purchases.cart(identity.current(session));
    }

    @PostMapping("/cart/{id}")
    public Views.Result addCart(@PathVariable long id, HttpSession session) {
        purchases.addCart(identity.current(session), id);
        return Views.Result.success();
    }

    @DeleteMapping("/cart/{id}")
    public Views.Result removeCart(@PathVariable long id, HttpSession session) {
        purchases.removeCart(identity.current(session), id);
        return Views.Result.success();
    }

    @PostMapping("/checkout")
    public Views.Purchase checkout(@RequestBody CheckoutInput input, HttpSession session) {
        return purchases.checkout(identity.current(session), input.method());
    }

    @GetMapping("/orders")
    public List<Views.Purchase> orders(HttpSession session) {
        return purchases.orders(identity.current(session));
    }

    @PostMapping("/orders/{id}/confirm")
    public Views.Result confirm(@PathVariable long id, HttpSession session) {
        purchases.confirm(identity.current(session), id);
        return Views.Result.success();
    }

    @PostMapping("/orders/{id}/cancel")
    public Views.Result cancel(@PathVariable long id, HttpSession session) {
        purchases.cancel(identity.current(session), id);
        return Views.Result.success();
    }
}
