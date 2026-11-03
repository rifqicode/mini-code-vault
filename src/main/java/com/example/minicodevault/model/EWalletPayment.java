package com.example.minicodevault.model;

import java.math.BigDecimal;

@org.springframework.stereotype.Component
@org.springframework.core.annotation.Order(3)
public final class EWalletPayment implements PaymentMethod {
    public String code() {
        return "EWALLET";
    }

    public String label() {
        return "E-wallet";
    }

    public BigDecimal fee(BigDecimal subtotal) {
        return new BigDecimal("2000");
    }

    public boolean requiresConfirmation() {
        return false;
    }
}
