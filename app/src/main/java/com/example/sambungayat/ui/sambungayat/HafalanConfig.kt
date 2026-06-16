package com.example.sambungayat.ui.sambungayat

/**
 * Konfigurasi urutan surah hafalan.
 *
 * - [order]       : posisi dalam urutan hafalan, mulai dari 1.
 * - [chapterId]   : id surah di database (tabel chapters).
 * - [chapterName] : nama surah sebagai fallback offline.
 *
 * Untuk mengubah urutan hafalan, cukup ubah list ini.
 * Tidak ada perubahan yang diperlukan di tempat lain.
 *
 * Semantik [highest_unlocked_surah] di user_progress:
 *   - Menyimpan nilai [order], BUKAN [chapterId].
 *   - User baru → highest_unlocked_surah = 1 → hanya Al-Fatihah terbuka.
 *   - User selesai Al-Fatihah → highest_unlocked_surah = 2 → An-Nas terbuka.
 */
data class HafalanSurah(
    val order       : Int,
    val chapterId   : Int,
    val chapterName : String
)

object HafalanConfig {

    // Urutan hafalan: Al-Fatihah → Juz 30 dari surah pendek ke panjang
    val HAFALAN_LIST: List<HafalanSurah> = listOf(
        HafalanSurah(order = 1,  chapterId = 1,   chapterName = "Al-Fatihah"),
        HafalanSurah(order = 2,  chapterId = 114, chapterName = "An-Nas"),
        HafalanSurah(order = 3,  chapterId = 113, chapterName = "Al-Falaq"),
        HafalanSurah(order = 4,  chapterId = 112, chapterName = "Al-Ikhlas"),
        HafalanSurah(order = 5,  chapterId = 111, chapterName = "Al-Masad"),
        HafalanSurah(order = 6,  chapterId = 110, chapterName = "An-Nasr"),
        HafalanSurah(order = 7,  chapterId = 109, chapterName = "Al-Kafirun"),
        HafalanSurah(order = 8,  chapterId = 108, chapterName = "Al-Kautsar"),
        HafalanSurah(order = 9,  chapterId = 107, chapterName = "Al-Maun"),
        HafalanSurah(order = 10, chapterId = 106, chapterName = "Quraisy"),
        HafalanSurah(order = 11, chapterId = 105, chapterName = "Al-Fil"),
        HafalanSurah(order = 12, chapterId = 104, chapterName = "Al-Humazah"),
        HafalanSurah(order = 13, chapterId = 103, chapterName = "Al-Asr"),
        HafalanSurah(order = 14, chapterId = 102, chapterName = "At-Takatsur"),
        HafalanSurah(order = 15, chapterId = 101, chapterName = "Al-Qariah"),
        HafalanSurah(order = 16, chapterId = 100, chapterName = "Al-Adiyat"),
        HafalanSurah(order = 17, chapterId = 99,  chapterName = "Az-Zalzalah"),
        HafalanSurah(order = 18, chapterId = 98,  chapterName = "Al-Bayyinah"),
        HafalanSurah(order = 19, chapterId = 96,  chapterName = "Al-Alaq"),
        HafalanSurah(order = 20, chapterId = 94,  chapterName = "Al-Insyirah"),
        HafalanSurah(order = 21, chapterId = 93,  chapterName = "Ad-Duha"),
        HafalanSurah(order = 22, chapterId = 92,  chapterName = "Al-Lail"),
        HafalanSurah(order = 23, chapterId = 91,  chapterName = "Asy-Syams"),
        HafalanSurah(order = 24, chapterId = 90,  chapterName = "Al-Balad"),
        HafalanSurah(order = 25, chapterId = 89,  chapterName = "Al-Fajr"),
        HafalanSurah(order = 26, chapterId = 88,  chapterName = "Al-Ghasyiyah"),
        HafalanSurah(order = 27, chapterId = 87,  chapterName = "Al-Ala"),
        HafalanSurah(order = 28, chapterId = 86,  chapterName = "At-Tariq"),
        HafalanSurah(order = 29, chapterId = 85,  chapterName = "Al-Buruj"),
        HafalanSurah(order = 30, chapterId = 78,  chapterName = "An-Naba")
    )

    val TOTAL = HAFALAN_LIST.size  // 30

    /** Ambil HafalanSurah berdasarkan posisi order (1-based). Null jika tidak ditemukan. */
    fun getByOrder(order: Int): HafalanSurah? =
        HAFALAN_LIST.find { it.order == order }

    /** Ambil posisi order dari chapterId. -1 jika surah tidak ada dalam daftar hafalan. */
    fun getOrderByChapterId(chapterId: Int): Int =
        HAFALAN_LIST.find { it.chapterId == chapterId }?.order ?: -1

    /** Ambil chapterId dari posisi order. -1 jika order di luar range. */
    fun getChapterIdByOrder(order: Int): Int =
        getByOrder(order)?.chapterId ?: -1

    /** Cek apakah suatu chapterId termasuk dalam daftar hafalan. */
    fun isHafalanSurah(chapterId: Int): Boolean =
        HAFALAN_LIST.any { it.chapterId == chapterId }
}
