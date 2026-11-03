package com.example.minicodevault.service;

import com.example.minicodevault.dto.Views;
import com.example.minicodevault.model.Learning;
import com.example.minicodevault.model.Order;
import com.example.minicodevault.model.User;
import com.example.minicodevault.repository.InMemoryStore;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;

@Service
public class PurchaseService {
    private final InMemoryStore store;
    private final ViewMapper mapper;
    private final PaymentRegistry payments;
    private final Clock clock;

    public PurchaseService(
            InMemoryStore store, ViewMapper mapper, PaymentRegistry payments, Clock clock) {
        this.store = store;
        this.mapper = mapper;
        this.payments = payments;
        this.clock = clock;
    }

    private boolean reserved(User user, long learningId) {
        return store.allOrders().stream()
                .anyMatch(order ->
                        order.getLearnerId() == user.id()
                                && order.getStatus() != Order.Status.CANCELLED
                                && order.getItems().stream()
                                        .anyMatch(course -> course.getId() == learningId));
    }

    private Learning purchasable(User user, long id) {
        Learning course = Authorization.learning(store, id);
        if (!course.isPurchasable())
            throw new IllegalArgumentException(
                    "Course "
                            + course.getTitle()
                            + " sedang tidak tersedia. Hapus dari keranjang jika sudah"
                            + " ditambahkan.");
        if (reserved(user, id)) throw new IllegalArgumentException("Course sudah dibeli.");
        return course;
    }

    public void addCart(User user, long id) {
        Authorization.requireRole(user, User.Role.LEARNER);
        store.transaction(() -> {
            purchasable(user, id);
            if (!store.addCart(user.id(), id))
                throw new IllegalArgumentException("Course sudah ada di keranjang.");
            return null;
        });
    }

    public void removeCart(User user, long id) {
        Authorization.requireRole(user, User.Role.LEARNER);
        store.removeCart(user.id(), id);
    }

    public Views.Cart cart(User user) {
        Authorization.requireRole(user, User.Role.LEARNER);
        return store.transaction(() -> {
            List<Learning> items =
                    store.cartIds(user.id()).stream()
                            .map(id -> Authorization.learning(store, id))
                            .toList();
            BigDecimal subtotal =
                    items.stream().map(Learning::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            return new Views.Cart(
                    items.stream().map(mapper::course).toList(),
                    subtotal,
                    payments.quotes(subtotal));
        });
    }

    public Views.Purchase checkout(User user, String method) {
        Authorization.requireRole(user, User.Role.LEARNER);
        return store.transaction(() -> {
            List<Long> ids = store.cartIds(user.id());
            if (ids.isEmpty()) throw new IllegalArgumentException("Keranjang masih kosong.");
            List<Learning> items = ids.stream().map(id -> purchasable(user, id)).toList();
            Order order =
                    new Order(
                            store.nextOrderId(),
                            user.id(),
                            items,
                            payments.find(method),
                            clock.instant());
            store.saveOrder(order);
            if (order.getStatus() == Order.Status.PAID) grant(order);
            store.clearCart(user.id());
            return mapper.purchase(order);
        });
    }

    private void grant(Order order) {
        order.getItems()
                .forEach(course ->
                        store.grantAccess(order.getLearnerId(), course, order.getPaidAt()));
    }

    public List<Views.Purchase> orders(User user) {
        return store.transaction(() ->
                store.allOrders().stream()
                        .filter(order ->
                                user.role() == User.Role.LEARNER
                                        ? order.getLearnerId() == user.id()
                                        : order.getItems().stream()
                                                .allMatch(course ->
                                                        course.getInstructorId() == user.id()))
                        .sorted(Comparator.comparingLong(Order::getId).reversed())
                        .map(mapper::purchase)
                        .toList());
    }

    public void confirm(User user, long id) {
        Authorization.requireRole(user, User.Role.INSTRUCTOR);
        store.transaction(() -> {
            Order order = Authorization.order(store, id);
            if (!order.getItems().stream()
                    .allMatch(course -> course.getInstructorId() == user.id())) {
                throw new AppException(403, "Transaksi ini bukan milik Anda.");
            }
            order.confirm(clock.instant());
            grant(order);
            return null;
        });
    }

    public void cancel(User user, long id) {
        Authorization.requireRole(user, User.Role.LEARNER);
        store.transaction(() -> {
            Order order = Authorization.order(store, id);
            if (order.getLearnerId() != user.id())
                throw new AppException(403, "Transaksi ini bukan milik Anda.");
            order.cancel();
            return null;
        });
    }
}
