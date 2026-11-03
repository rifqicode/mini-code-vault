# CodeVault

Aplikasi web pembelian konten pembelajaran menggunakan Java 21, Spring Boot 4.1.2, Spring MVC, Thymeleaf, dan CSS

Akun hardcoded dan data disimpan dalam memori, tanpa database. Semua perubahan, keranjang, pembelian, serta ulasan akan direset saat server dimulai ulang.

## Menjalankan

Memerlukan JDK 21 atau versi lebih baru yang didukung Spring Boot, Maven 3.6.3+, serta internet untuk unduhan dependensi pertama.

```sh
mvn clean spring-boot:run
```

```sh
PORT=8081 mvn clean spring-boot:run
```

## Akun contoh

| Username     | Password  | Role          |
| ------------ | --------- | ------------- |
| `instructor` | `demo123` | Instruktur    |
| `learner`    | `demo123` | Learner       |
| `learner2`   | `demo123` | Learner kedua |

Login memakai session server. Akun kedua disediakan untuk mencoba pemisahan keranjang, pembelian, dan hak akses.

## Fitur

- Instruktur: tambah/edit course, ubah harga dan ketersediaan, atur Draft/Dipublikasikan/Ditangguhkan, arsipkan course, lihat transaksi, laporan penjualan, dan ulasan.
- Learner: cari/filter katalog, lihat detail course, kelola keranjang melalui tombol mengambang, checkout di panel keranjang, lihat riwayat dan koleksi, akses materi, serta tambah/edit ulasan.
- Tampilan putih polos tanpa mode gelap. Keranjang dapat ditutup melalui tombol Tutup, Escape, atau klik di luar panel.
- Modul E-Book: isi materi menjadi file `.txt` yang diunduh sesuai jatah per pembeli.
- Video Course: masa akses sejak pembayaran berhasil, total durasi, daftar chapter, dan pemutar video melalui endpoint yang memeriksa kepemilikan.
- Coding Test: daftar soal dengan tingkat EASY/MEDIUM/HARD dan pembahasan yang dapat dibuka berulang kali; tanpa editor atau eksekusi kode.


## Struktur

```text
src/main/java/com/example/minicodevault/
├── config/          # Konfigurasi, data contoh, penjagaan request
├── controller/      # Endpoint API dan identitas session
│   └── web/         # Controller MVC, data form, dan formatter tampilan
├── dto/             # Respons API bertipe
├── model/           # Entitas dan aturan domain
├── repository/      # Penyimpanan dalam memori
└── service/         # Autentikasi, katalog, transaksi, akses, dan laporan

src/main/resources/
├── application.properties
├── media/           # Video lokal; tidak disajikan langsung sebagai file publik
├── templates/
│   ├── auth/        # Login
│   ├── catalog/     # Katalog dan detail course
│   ├── learner/     # Keranjang, koleksi, materi, riwayat, dan ulasan
│   ├── instructor/  # Pengelolaan course, laporan, dan ulasan
│   ├── fragments/   # Layout, tombol pembelian, dan panel keranjang bersama
│   └── error.html
└── static/
    ├── styles.css
    └── interactions.js
```