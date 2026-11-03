package com.example.minicodevault.config;

import com.example.minicodevault.model.CodingTest;
import com.example.minicodevault.model.EBook;
import com.example.minicodevault.model.Learning;
import com.example.minicodevault.model.User;
import com.example.minicodevault.model.VideoCourse;
import com.example.minicodevault.repository.InMemoryStore;

import java.math.BigDecimal;
import java.util.List;

public final class DemoData {
    private DemoData() {}

    public static void load(InMemoryStore store) {
        store.saveUser(new User(1, "Rifqi", "instructor", User.Role.INSTRUCTOR));
        store.saveUser(new User(2, "Pelajar", "learner", User.Role.LEARNER));
        store.saveUser(new User(3, "Pelajar Kedua", "learner2", User.Role.LEARNER));
        store.saveLearning(
                new EBook(
                        1,
                        1,
                        "Panduan Data Structures & Algorithms",
                        "Modul contoh untuk memahami array, pencarian, dan kompleksitas algoritma."
                                + " Materi dapat diunduh sebagai file teks.",
                        new BigDecimal("79000"),
                        Learning.Status.PUBLISHED,
                        true,
                        3,
                        "PANDUAN DATA STRUCTURES & ALGORITHMS\n"
                            + "Materi contoh CodeVault\n\n"
                            + "1. Array\n"
                            + "Array menyimpan elemen berurutan dan memungkinkan akses indeks dalam"
                            + " O(1).\n\n"
                            + "2. Linear Search\n"
                            + "Periksa setiap elemen sampai target ditemukan. Kompleksitas waktu"
                            + " O(n).\n\n"
                            + "3. Binary Search\n"
                            + "Pada array terurut, bagi area pencarian menjadi dua setiap langkah."
                            + " Kompleksitas O(log n).\n\n"
                            + "Latihan: implementasikan pencarian angka pada array terurut dan"
                            + " bandingkan kedua pendekatan."));
        store.saveLearning(
                new VideoCourse(
                        2,
                        1,
                        "Full-Stack Web Development",
                        "Course contoh React & Node.js dengan empat chapter. Pemutar menggunakan"
                                + " klip demonstrasi lokal, bukan rekaman pelatihan lengkap.",
                        new BigDecimal("249000"),
                        Learning.Status.PUBLISHED,
                        true,
                        90,
                        120,
                        List.of(
                                "Menyiapkan proyek",
                                "Komponen dan state React",
                                "REST API dengan Node.js",
                                "Menghubungkan frontend dan backend")));
        store.saveLearning(
                new CodingTest(
                        3,
                        1,
                        "Interview Preparation: Backend Developer",
                        "Tiga soal contoh untuk latihan mandiri. Pelajari pertanyaan, susun jawaban"
                            + " sendiri, lalu buka pembahasannya. Tanpa eksekusi kode otomatis.",
                        new BigDecimal("99000"),
                        Learning.Status.PUBLISHED,
                        true,
                        List.of(
                                new CodingTest.Question(
                                        "Membalik string",
                                        CodingTest.Difficulty.EASY,
                                        "Bagaimana membalik string Java tanpa mengubah string"
                                                + " aslinya?",
                                        "Gunakan new StringBuilder(input).reverse().toString()."
                                                + " String bersifat immutable; hasilnya merupakan"
                                                + " string baru. Kompleksitas waktu O(n)."),
                                new CodingTest.Question(
                                        "Two Sum",
                                        CodingTest.Difficulty.MEDIUM,
                                        "Diberikan array integer dan target, temukan dua indeks"
                                                + " dengan nilai yang berjumlah target.",
                                        "Telusuri array sambil menyimpan nilai dan indeks dalam"
                                            + " HashMap. Cari komplemen target - nilai sebelum"
                                            + " memasukkan elemen saat ini. Waktu rata-rata O(n),"
                                            + " ruang O(n)."),
                                new CodingTest.Question(
                                        "LRU Cache",
                                        CodingTest.Difficulty.HARD,
                                        "Rancang cache dengan get dan put O(1) yang membuang elemen"
                                                + " paling lama tidak digunakan.",
                                        "Gabungkan HashMap dan doubly linked list. Map menunjuk"
                                            + " node, sedangkan list mengurutkan penggunaan."
                                            + " Pindahkan node yang diakses ke depan; buang node"
                                            + " belakang ketika kapasitas terlampaui."))));
    }
}
