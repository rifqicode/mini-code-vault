package com.example.minicodevault.service;

import com.example.minicodevault.config.DemoData;
import com.example.minicodevault.dto.Views;
import com.example.minicodevault.model.BankTransferPayment;
import com.example.minicodevault.model.CardPayment;
import com.example.minicodevault.model.ContentAccess;
import com.example.minicodevault.model.EWalletPayment;
import com.example.minicodevault.model.Order;
import com.example.minicodevault.model.User;
import com.example.minicodevault.repository.InMemoryStore;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class ServiceChecks {
    private static final Instant NOW = Instant.parse("2026-11-03T00:00:00Z");
    private static int count;

    private record Fixture(
            InMemoryStore store,
            AuthService auth,
            CatalogService catalog,
            PurchaseService purchases,
            AccessService access,
            ReportService reports) {}

    private static Fixture fixture(Clock clock) {
        InMemoryStore store = new InMemoryStore();
        DemoData.load(store);
        ViewMapper mapper = new ViewMapper(store, clock);
        PaymentRegistry payments =
                new PaymentRegistry(
                        List.of(
                                new BankTransferPayment(),
                                new CardPayment(),
                                new EWalletPayment()));
        return new Fixture(
                store,
                new AuthService(store),
                new CatalogService(store, mapper, new LearningFactory()),
                new PurchaseService(store, mapper, payments, clock),
                new AccessService(store, mapper, clock),
                new ReportService(store, payments));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        count++;
        System.out.println("PASS " + message);
    }

    private static void rejected(Runnable operation, String message) {
        try {
            operation.run();
        } catch (IllegalArgumentException | AppException expected) {
            check(true, message);
            return;
        }
        throw new AssertionError("Operation should fail: " + message);
    }

    private static LearningInput ebook(
            String title, String price, String status, boolean available) {
        return new LearningInput(
                "EBOOK",
                title,
                "Materi pengujian",
                new BigDecimal(price),
                status,
                available,
                7,
                "Materi baru",
                null,
                null,
                null,
                null);
    }

    private static void purchaseAndAccessChecks() {
        Fixture f = fixture(Clock.fixed(NOW, ZoneOffset.UTC));
        User learner = f.auth.login("learner", "demo123");
        User other = f.auth.login("learner2", "demo123");
        User instructor = f.auth.login("instructor", "demo123");

        rejected(() -> f.auth.login("learner", "wrong"), "password salah ditolak");
        rejected(() -> f.purchases.addCart(instructor, 1), "instruktur tidak dapat checkout");
        check(f.catalog.catalog().size() == 3, "tiga subtype Learning tampil pada satu katalog");
        check(
                f.catalog.detail(1).course().type().equals("EBOOK"),
                "detail katalog memakai DTO metadata publik");
        check(
                f.catalog.detail(3).difficulties().size() == 3,
                "katalog hanya memuat tingkat soal, bukan solusi");
        rejected(() -> f.purchases.checkout(learner, "CARD"), "keranjang kosong ditolak");
        f.purchases.addCart(learner, 1);
        rejected(() -> f.purchases.addCart(learner, 1), "duplikasi keranjang ditolak");
        Views.Purchase order = f.purchases.checkout(learner, "CARD");
        check(new BigDecimal("1975").equals(order.fee()), "biaya kartu 2,5 persen dihitung server");
        check(new BigDecimal("80975").equals(order.total()), "total kartu benar");
        check(
                f.access.library(learner).size() == 1,
                "kartu membuka akses setelah simulasi berhasil");
        rejected(() -> f.purchases.addCart(learner, 1), "pembelian ganda ditolak");
        rejected(() -> f.access.download(other, 1), "learner lain tidak dapat mengunduh");
        rejected(
                () -> f.access.review(other, 1, 5, "Belum beli"), "ulasan tanpa pembelian ditolak");

        f.catalog.saveLearning(
                instructor, 1L, ebook("Judul diperbarui", "100000.00", "PUBLISHED", true));
        check(
                new BigDecimal("79000").equals(f.purchases.orders(learner).getFirst().subtotal()),
                "harga transaksi lama tidak berubah");
        check(
                f.access.download(learner, 1).contains("Panduan Data Structures"),
                "materi pembelian lama tetap snapshot");
        f.access.download(learner, 1);
        f.access.download(learner, 1);
        rejected(
                () -> f.access.download(learner, 1),
                "unduhan keempat ditolak walau batas konten baru berubah");
        f.access.review(learner, 1, 4, "Mudah dipahami");
        f.access.review(learner, 1, 5, "Ulasan diperbarui");
        check(f.catalog.detail(1).reviews().size() == 1, "ulasan diperbarui tanpa duplikasi");
        check(
                f.access.library(learner).getFirst().myReview().rating() == 5,
                "ulasan pribadi tersedia untuk edit");
        rejected(
                () -> f.access.review(learner, 1, 6, "Tidak valid"),
                "rating di luar skala ditolak");

        f.purchases.addCart(learner, 2);
        Views.Purchase bank = f.purchases.checkout(learner, "BANK");
        check(
                bank.status() == Order.Status.PAID && BigDecimal.ZERO.equals(bank.fee()),
                "transfer langsung lunas tanpa biaya");
        check(f.access.lesson(learner, 2).access().active(), "transfer langsung membuka materi");
        rejected(
                () -> f.purchases.confirm(learner, bank.id()),
                "learner tidak dapat mengonfirmasi transfer");
        rejected(
                () -> f.purchases.cancel(other, bank.id()),
                "learner lain tidak dapat membatalkan transaksi");
        check(
                f.access
                        .lesson(learner, 2)
                        .access()
                        .expiresAt()
                        .equals(NOW.plusSeconds(90L * 86400)),
                "akses video dimulai saat pembayaran otomatis diterima");
        rejected(() -> f.purchases.confirm(instructor, bank.id()), "transaksi lunas tidak perlu konfirmasi ulang");
        f.access.checkVideo(learner, 2);
        ContentAccess video = new ContentAccess(f.store.findLearning(2).orElseThrow(), NOW);
        check(
                video.isActive(NOW.plusSeconds(90L * 86400 - 1)),
                "video aktif sesaat sebelum kedaluwarsa");
        check(
                !video.isActive(NOW.plusSeconds(90L * 86400)),
                "video tidak aktif tepat pada waktu kedaluwarsa");

        f.purchases.addCart(learner, 3);
        Views.Purchase wallet = f.purchases.checkout(learner, "EWALLET");
        check(new BigDecimal("2000").equals(wallet.fee()), "biaya e-wallet flat per transaksi");
        check(
                f.access.lesson(learner, 3).questions().size() == 3,
                "coding menampilkan soal dan pembahasan");
        f.access.lesson(learner, 3);
        f.access.lesson(learner, 3);
        check(
                f.access.library(learner).stream()
                        .filter(a -> a.course().type().equals("CODING"))
                        .findFirst()
                        .orElseThrow()
                        .active(),
                "coding bisa diakses berulang kali");
        f.catalog.deleteLearning(instructor, 3);
        check(
                f.catalog.catalog().stream().noneMatch(course -> course.id() == 3),
                "course arsip hilang dari katalog");
        check(
                f.access.lesson(learner, 3).questions().size() == 3,
                "arsip mempertahankan akses pembeli");
        f.access.review(learner, 3, 4, "Tetap bisa memberi ulasan setelah arsip");
        check(
                f.access.library(learner).stream()
                                .filter(a -> a.course().id() == 3)
                                .findFirst()
                                .orElseThrow()
                                .myReview()
                        != null,
                "edit ulasan mendukung konten arsip");

        f.purchases.addCart(other, 1);
        check(
                !f.purchases.cart(other).items().isEmpty(),
                "keranjang learner kedua terpisah dari learner pertama");
        f.catalog.saveLearning(
                instructor, 1L, ebook("Tidak tersedia", "100000", "SUSPENDED", false));
        rejected(
                () -> f.purchases.checkout(other, "CARD"),
                "checkout memeriksa ketersediaan terbaru");
        rejected(
                () ->
                        f.catalog.saveLearning(
                                learner, null, ebook("Bukan seller", "1", "DRAFT", true)),
                "learner tidak dapat membuat konten");
        long draft = f.catalog.saveLearning(instructor, null, ebook("Draft", "1", "DRAFT", true));
        check(
                f.catalog.catalog().stream().noneMatch(course -> course.id() == draft),
                "draft tidak muncul di katalog");
        rejected(
                () ->
                        f.catalog.saveLearning(
                                instructor, null, ebook("Negatif", "-1", "DRAFT", true)),
                "harga negatif ditolak");
        Views.Report report = f.reports.report(instructor);
        check(report.transactionCount() == 3, "laporan hanya menghitung transaksi lunas");
        check(
                new BigDecimal("427000").equals(report.revenue()),
                "pendapatan tidak memasukkan biaya admin atau transaksi batal");
        check(new BigDecimal("3975").equals(report.fees()), "biaya admin dilaporkan terpisah");
        check(
                new CardPayment().fee(new BigDecimal("100020")).equals(new BigDecimal("2501")),
                "biaya setengah rupiah dibulatkan HALF_UP");
    }

    private static void concurrencyChecks() throws Exception {
        Fixture f = fixture(Clock.fixed(NOW, ZoneOffset.UTC));
        User learner = f.auth.login("learner", "demo123");
        f.purchases.addCart(learner, 1);
        try (var executor = Executors.newFixedThreadPool(6)) {
            List<Callable<Boolean>> checkouts = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                checkouts.add(() -> {
                    try {
                        f.purchases.checkout(learner, "CARD");
                        return true;
                    } catch (IllegalArgumentException expected) {
                        return false;
                    }
                });
            }
            int successful = 0;
            for (var result : executor.invokeAll(checkouts)) if (result.get()) successful++;
            check(
                    successful == 1 && f.purchases.orders(learner).size() == 1,
                    "checkout bersamaan tidak membuat pembelian ganda");
            List<Callable<Boolean>> downloads = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                downloads.add(() -> {
                    try {
                        f.access.download(learner, 1);
                        return true;
                    } catch (IllegalArgumentException expected) {
                        return false;
                    }
                });
            }
            int accepted = 0;
            for (var result : executor.invokeAll(downloads)) if (result.get()) accepted++;
            check(
                    accepted == 3 && f.access.library(learner).getFirst().remainingDownloads() == 0,
                    "unduhan bersamaan tidak melewati kuota");
        }
    }

    public static void main(String[] args) throws Exception {
        purchaseAndAccessChecks();
        concurrencyChecks();
        System.out.println("All " + count + " checks passed.");
    }
}
