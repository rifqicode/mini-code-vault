package com.example.minicodevault.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

@org.springframework.stereotype.Component
@org.springframework.core.annotation.Order(2)
public final class CardPayment implements PaymentMethod {
    public String code() {
        return "CARD";
    }

    public String label() {
        return "Kartu kredit/debit";
    }

    public BigDecimal fee(BigDecimal subtotal) {
        return subtotal.multiply(new BigDecimal("0.025")).setScale(0, RoundingMode.HALF_UP);
    }

    public boolean requiresConfirmation() {
        return false;
    }
}
