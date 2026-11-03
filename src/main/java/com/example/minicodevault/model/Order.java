package com.example.minicodevault.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Order {
    public enum Status {
        PENDING,
        PAID,
        CANCELLED
    }

    private final long id;
    private final long learnerId;
    private final List<Learning> items;
    private final String paymentCode;
    private final BigDecimal subtotal;
    private final BigDecimal fee;
    private final Instant createdAt;
    private Status status;
    private Instant paidAt;

    public Order(
            long id, long learnerId, List<Learning> items, PaymentMethod payment, Instant now) {
        if (items.isEmpty()) throw new IllegalArgumentException("Keranjang masih kosong.");
        this.id = id;
        this.learnerId = learnerId;
        this.items = items.stream().map(Learning::snapshot).toList();
        this.paymentCode = payment.code();
        this.subtotal =
                items.stream().map(Learning::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.fee = payment.fee(subtotal);
        this.createdAt = now;
        this.status = payment.requiresConfirmation() ? Status.PENDING : Status.PAID;
        this.paidAt = status == Status.PAID ? now : null;
    }

    public void confirm(Instant now) {
        if (status != Status.PENDING)
            throw new IllegalArgumentException("Transaksi sudah diproses.");
        status = Status.PAID;
        paidAt = now;
    }

    public void cancel() {
        if (status != Status.PENDING)
            throw new IllegalArgumentException("Hanya transaksi tertunda yang bisa dibatalkan.");
        status = Status.CANCELLED;
    }

    public long getId() {
        return id;
    }

    public long getLearnerId() {
        return learnerId;
    }

    public List<Learning> getItems() {
        return items;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public BigDecimal getTotal() {
        return subtotal.add(fee);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Status getStatus() {
        return status;
    }
}
