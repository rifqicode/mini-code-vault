package com.example.minicodevault.model;

import java.math.BigDecimal;

public interface PaymentMethod {
    String code();

    String label();

    BigDecimal fee(BigDecimal subtotal);

    boolean requiresConfirmation();
}
