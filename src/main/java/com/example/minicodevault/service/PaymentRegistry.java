package com.example.minicodevault.service;

import com.example.minicodevault.dto.Views;
import com.example.minicodevault.model.PaymentMethod;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentRegistry {
    private final List<PaymentMethod> methods;

    public PaymentRegistry(List<PaymentMethod> methods) {
        this.methods = List.copyOf(methods);
    }

    public PaymentMethod find(String code) {
        return methods.stream()
                .filter(method -> method.code().equals(code))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Pilih metode pembayaran yang valid."));
    }

    public List<Views.PaymentOption> options() {
        return methods.stream()
                .map(method ->
                        new Views.PaymentOption(
                                method.code(), method.label(), method.requiresConfirmation()))
                .toList();
    }

    public Map<String, Views.Quote> quotes(BigDecimal subtotal) {
        Map<String, Views.Quote> quotes = new LinkedHashMap<>();
        for (PaymentMethod method : methods) {
            BigDecimal fee = method.fee(subtotal);
            quotes.put(method.code(), new Views.Quote(fee, subtotal.add(fee)));
        }
        return Map.copyOf(quotes);
    }
}
