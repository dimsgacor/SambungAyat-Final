package com.example.sambungayat.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sambungayat.R
import com.example.sambungayat.databinding.FragmentHomeBinding
import com.example.sambungayat.network.ApiResult
import com.example.sambungayat.network.SessionManager
import com.example.sambungayat.network.model.response.LeaderboardResponse
import com.example.sambungayat.network.repository.QuranRepository
import com.example.sambungayat.network.repository.UserRepository
import com.example.sambungayat.ui.game.GameActivity
import com.example.sambungayat.ui.sambungayat.HafalanConfig
import com.example.sambungayat.ui.sambungayat.SambungAyatListActivity
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val userRepository  = UserRepository()
    private val quranRepository = QuranRepository()
    private lateinit var sessionManager: SessionManager

    // Data untuk Continue Learning
    private var continueChapterId   = 1
    private var continueChapterName = ""
    private var continueVerseCount  = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        binding.btnSambungAyat.setOnClickListener {
            startActivity(Intent(requireContext(), SambungAyatListActivity::class.java))
        }

        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val userId = sessionManager.getUserId()
        binding.tvWelcomeUser.text = "Assalamu Alaikum, ${sessionManager.getUsername()}!"

        // Tampilkan cache lokal langsung
        binding.tvTotalScore.text = sessionManager.getTotalScore().toString()
        binding.tvBestStreak.text = sessionManager.getBestStreak().toString()
        updateHafalanProgress(sessionManager.getHighestSurah())
        setupContinueFromCache()

        viewLifecycleOwner.lifecycleScope.launch {

            // Statistics
            when (val result = userRepository.getStatistics(userId)) {
                is ApiResult.Success -> {
                    val d = result.data
                    binding.tvTotalScore.text = d.totalScore.toString()
                    binding.tvBestStreak.text = d.bestStreak.toString()
                    updateHafalanProgress(d.highestUnlockedSurah)
                    sessionManager.saveProgress(
                        totalScore           = d.totalScore,
                        bestStreak           = d.bestStreak,
                        currentSurah         = d.currentSurah,
                        currentVerse         = sessionManager.getCurrentVerse(),
                        highestUnlockedSurah = d.highestUnlockedSurah
                    )
                }
                else -> {}
            }

            // Continue Learning — resolve nama surah dari HafalanConfig + API
            when (val result = userRepository.getProgress(userId)) {
                is ApiResult.Success -> {
                    val d = result.data
                    // currentSurah di DB menyimpan chapterId (bukan order)
                    val storedChapterId = d.currentSurah
                    val currentVerse    = d.currentVerse

                    // Ambil nama surah dari detail API
                    loadContinueSurahName(storedChapterId, currentVerse)

                    val highestOrder = d.highestUnlockedSurah.coerceAtLeast(1)
                    val pct = ((highestOrder.toFloat() / HafalanConfig.TOTAL.toFloat()) * 100).toInt()
                    binding.progressContinue.progress = pct
                }
                else -> setupContinueFromCache()
            }

            // Leaderboard
            when (val result = quranRepository.getLeaderboard(5)) {
                is ApiResult.Success -> buildLeaderboard(result.data)
                else -> {}
            }
        }
    }

    /**
     * Fallback ke cache lokal saat API belum tersedia.
     * currentSurah di cache menyimpan chapterId.
     */
    private fun setupContinueFromCache() {
        val chapterId = sessionManager.getCurrentSurah()
        val hafalanSurah = HafalanConfig.HAFALAN_LIST.find { it.chapterId == chapterId }

        continueChapterId   = chapterId
        continueChapterName = hafalanSurah?.chapterName ?: "Surah $chapterId"
        continueVerseCount  = 0

        binding.tvContinueSurah.text =
            "$continueChapterName · Ayat ${sessionManager.getCurrentVerse()}"

        binding.cardContinue.setOnClickListener {
            openGame(continueChapterId, continueChapterName, continueVerseCount)
        }
    }

    private suspend fun loadContinueSurahName(chapterId: Int, currentVerse: Int) {
        continueChapterId = chapterId

        when (val result = quranRepository.getSurahDetail(chapterId)) {
            is ApiResult.Success -> {
                continueChapterName = result.data.surah.name
                continueVerseCount  = result.data.surah.verseCount
                binding.tvContinueSurah.text = "${result.data.surah.name} · Ayat $currentVerse"
            }
            else -> {
                val hafalanSurah = HafalanConfig.HAFALAN_LIST.find { it.chapterId == chapterId }
                continueChapterName = hafalanSurah?.chapterName ?: "Surah $chapterId"
                binding.tvContinueSurah.text = "$continueChapterName · Ayat $currentVerse"
            }
        }

        binding.cardContinue.setOnClickListener {
            openGame(continueChapterId, continueChapterName, continueVerseCount)
        }
    }

    /**
     * Continue Learning → langsung buka GameActivity.
     * Membuka Mode Sambung Ayat pada surah yang sedang dikerjakan user.
     */
    private fun openGame(chapterId: Int, chapterName: String, verseCount: Int) {
        startActivity(
            Intent(requireContext(), GameActivity::class.java).apply {
                putExtra(GameActivity.EXTRA_CHAPTER_ID,   chapterId)
                putExtra(GameActivity.EXTRA_CHAPTER_NAME, chapterName)
                putExtra(GameActivity.EXTRA_VERSE_COUNT,  verseCount)
            }
        )
    }

    /**
     * Progress Hafalan dihitung dari highestOrder:
     * - Surah selesai = highestOrder - 1
     * - Total = HafalanConfig.TOTAL (30 surah)
     */
    private fun updateHafalanProgress(highestOrder: Int) {
        val done = (highestOrder - 1).coerceAtLeast(0)
        val pct  = ((done.toFloat() / HafalanConfig.TOTAL.toFloat()) * 100).toInt()
        binding.tvHafalanCount.text   = "$done / ${HafalanConfig.TOTAL} Surah Selesai"
        binding.tvHafalanPercent.text = "$pct%"
        binding.progressHafalan.progress = pct
    }

    private fun buildLeaderboard(items: List<LeaderboardResponse>) {
        val container = binding.layoutLeaderboard
        container.removeAllViews()

        if (items.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text = "Belum ada data leaderboard"
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface_variant))
            }
            container.addView(empty)
            return
        }

        items.forEachIndexed { index, item ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = Gravity.CENTER_VERTICAL
                setPadding(0, 12, 0, 12)
            }

            val tvRank = TextView(requireContext()).apply {
                text     = "#${item.rank}"
                textSize = 14f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(
                    if (item.rank == 1)
                        ContextCompat.getColor(requireContext(), R.color.secondary)
                    else
                        ContextCompat.getColor(requireContext(), R.color.on_surface_variant)
                )
                minWidth = 48
            }

            val tvName = TextView(requireContext()).apply {
                text         = item.name
                textSize     = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface))
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                )
            }

            val tvScore = TextView(requireContext()).apply {
                text     = item.totalScore.toString()
                textSize = 14f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
                gravity  = Gravity.END
            }

            row.addView(tvRank)
            row.addView(tvName)
            row.addView(tvScore)
            container.addView(row)

            if (index < items.size - 1) {
                container.addView(View(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    )
                    setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.outline_variant)
                    )
                })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
