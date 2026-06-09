# Sambung Ayat Mobile

Aplikasi Android untuk latihan hafalan Al-Qur'an dengan mekanisme **Drag & Drop Sambung Ayat**.

## Deskripsi

Pengguna memilih surah, kemudian menyusun potongan kata dari ayat yang telah diacak menggunakan metode drag and drop. Sistem akan memeriksa urutan jawaban dan memberikan poin berdasarkan hasil yang diperoleh.

Aplikasi ini dibuat sebagai proyek UAS Pemrograman Mobile.

---

## Fitur

### Game Logic

* Drag and Drop kata ayat
* Validasi jawaban
* Sistem poin
* Combo system
* Streak system
* Sistem kelulusan surah
* Unlock surah berikutnya

### Statistik

* Total poin
* Combo saat ini
* Streak saat ini
* Best streak

---

## Teknologi

* Kotlin
* Android Studio
* RecyclerView
* ItemTouchHelper
* ViewModel
* LiveData
* ViewBinding
* MVVM Architecture

---

## Struktur Project

```text
com.example.sambungayat
│
├── logic
│   ├── ValidationManager
│   ├── ScoreManager
│   ├── ComboManager
│   ├── StreakManager
│   ├── SurahProgressManager
│   └── GameManager
│
├── model
│   └── GameState
│
├── viewmodel
│   └── GameViewModel
│
└── ui
    ├── GameActivity
    ├── WordAdapter
    └── DragDropCallback
```

---

## Cara Menjalankan

1. Clone repository

```bash
git clone https://github.com/furky1246/Uas-pemob.git
```

2. Buka project menggunakan Android Studio

3. Sync Gradle

4. Jalankan aplikasi pada emulator atau perangkat Android

---

## Status Pengembangan

### Selesai

* Drag & Drop
* Validasi Jawaban
* Sistem Poin
* Combo
* Streak
* Sistem Kelulusan

### Pengembangan Selanjutnya

* Integrasi API Al-Qur'an
* Login Google
* Leaderboard
* Statistik Hafalan
* Audio Ayat
* Progress Penyimpanan Database

---

## Tim Pengembang

### Game Logic Developer

* Dimas Bain Sentosa

### Project Team

* Kelompok UAS Pemrograman Mobile

---

## Lisensi

Digunakan untuk kebutuhan akademik dan pembelajaran.
