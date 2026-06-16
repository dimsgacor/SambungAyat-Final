package com.example.sambungayat.ui.sambungayat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sambungayat.databinding.ActivitySambungAyatListBinding
import com.example.sambungayat.network.ApiResult
import com.example.sambungayat.network.SessionManager
import com.example.sambungayat.network.repository.QuranRepository
import kotlinx.coroutines.launch

class SambungAyatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySambungAyatListBinding
    private val repository = QuranRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySambungAyatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        binding.btnBack.setOnClickListener { finish() }
        binding.rvSurah.layoutManager = LinearLayoutManager(this)

        loadSurahList()
    }

    override fun onResume() {
        super.onResume()
        // Refresh status unlock setiap kali kembali dari GameActivity
        loadSurahList()
    }

    private fun loadSurahList() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            // Ambil verse count dari API untuk semua chapter_id yang ada di HAFALAN_LIST
            // Gunakan getChapters() untuk satu request saja
            val verseCountMap = mutableMapOf<Int, Int>()

            when (val result = repository.getChapters()) {
                is ApiResult.Success -> {
                    result.data.forEach { chapter ->
                        verseCountMap[chapter.id] = chapter.verseCount
                    }
                }
                is ApiResult.Error -> {
                    // Lanjut tanpa verseCount — adapter handle dengan "? ayat"
                }
                else -> {}
            }

            binding.progressBar.visibility = View.GONE

            // highestOrder = posisi order tertinggi yang terbuka
            // Default 1 = hanya Al-Fatihah yang terbuka untuk user baru
            val highestOrder = sessionManager.getHighestSurah().coerceAtLeast(1)

            val adapter = SurahProgressAdapter(
                items         = HafalanConfig.HAFALAN_LIST,
                highestOrder  = highestOrder,
                verseCountMap = verseCountMap
            )
            binding.rvSurah.adapter = adapter

            // Scroll ke surah yang sedang aktif
            val activeIndex = HafalanConfig.HAFALAN_LIST
                .indexOfFirst { it.order == highestOrder }
            if (activeIndex > 0) {
                binding.rvSurah.scrollToPosition(activeIndex)
            }
        }
    }
}
