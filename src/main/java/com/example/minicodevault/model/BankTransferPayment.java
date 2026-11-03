package com.example.minicodevault.model;

import java.math.BigDecimal;

@org.springframework.stereotype.Component
@org.springframework.core.annotation.Order(1)
public final class BankTransferPayment implements PaymentMethod {
    public String code() {
        return "BANK";
    }

    public String label() {
        return "Transfer bank";
    }

    public BigDecimal fee(BigDecimal subtotal) {
        return BigDecimal.ZERO;
    }

    public boolean requiresConfirmation() {
        return false;
    }
}
