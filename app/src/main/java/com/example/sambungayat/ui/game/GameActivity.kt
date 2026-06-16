package com.example.sambungayat.ui.game

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sambungayat.databinding.ActivityGameBinding
import com.example.sambungayat.gamelogic.GameState
import com.example.sambungayat.gamelogic.GameViewModel
import com.example.sambungayat.network.ApiResult
import com.example.sambungayat.network.SessionManager
import com.example.sambungayat.network.repository.QuranRepository
import com.example.sambungayat.network.repository.UserRepository
import com.example.sambungayat.ui.sambungayat.HafalanConfig
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHAPTER_ID   = "chapter_id"
        const val EXTRA_CHAPTER_NAME = "chapter_name"
        const val EXTRA_VERSE_COUNT  = "verse_count"
    }

    private lateinit var binding: ActivityGameBinding
    private val viewModel: GameViewModel by viewModels()

    private lateinit var wordAdapter: WordAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var sessionManager: SessionManager

    private val quranRepository = QuranRepository()
    private val userRepository  = UserRepository()

    private var chapterId   = 1
    private var chapterName = ""
    private var verseCount  = 0

    private var ayatList: List<String> = emptyList()
    private var currentAyatIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        chapterId   = intent.getIntExtra(EXTRA_CHAPTER_ID, 1)
        chapterName = intent.getStringExtra(EXTRA_CHAPTER_NAME) ?: ""
        verseCount  = intent.getIntExtra(EXTRA_VERSE_COUNT, 0)

        binding.tvSurahName.text = chapterName
        binding.btnBack.setOnClickListener { finish() }

        setupRecyclerView()
        setupObservers()
        setupListeners()
        loadProgressThenStart()
    }

    private fun loadProgressThenStart() {
        setLoadingState(true)

        lifecycleScope.launch {
            val savedScore  = sessionManager.getTotalScore()
            val savedStreak = sessionManager.getBestStreak()

            viewModel.initFromBackend(savedScore, savedStreak)
            binding.tvScore.text  = savedScore.toString()
            binding.tvStreak.text = savedStreak.toString()

            fetchVersesAndStart()
        }
    }

    private fun fetchVersesAndStart() {
        lifecycleScope.launch {
            when (val result = quranRepository.getVerses(chapterId)) {
                is ApiResult.Success -> {
                    ayatList = result.data
                        .sortedBy { it.verseNumber }
                        .mapNotNull { it.text }
                        .filter { it.isNotBlank() }

                    if (ayatList.isEmpty()) {
                        Toast.makeText(this@GameActivity, "Tidak ada ayat tersedia", Toast.LENGTH_SHORT).show()
                        finish()
                        return@launch
                    }

                    setLoadingState(false)
                    viewModel.setTotalSurahInApp(HafalanConfig.TOTAL)
                    viewModel.startSurah(chapterId, ayatList.size)
                    loadCurrentAyat()
                }
                is ApiResult.Error -> {
                    setLoadingState(false)
                    Toast.makeText(this@GameActivity, result.message, Toast.LENGTH_SHORT).show()
                    finish()
                }
                else -> {}
            }
        }
    }

    private fun setupRecyclerView() {
        wordAdapter = WordAdapter(
            words = mutableListOf(),
            onDragStarted = { viewHolder -> itemTouchHelper.startDrag(viewHolder) }
        )
        val callback = DragDropCallback(
            adapter = wordAdapter,
            onMoveFinished = { from, to -> viewModel.onDragAndDrop(from, to) }
        )
        itemTouchHelper = ItemTouchHelper(callback)
        binding.rvWords.apply {
            layoutManager = LinearLayoutManager(this@GameActivity)
            adapter = wordAdapter
            itemTouchHelper.attachToRecyclerView(this)
        }
    }

    private fun setupObservers() {
        viewModel.gameState.observe(this) { state ->
            when (state) {
                is GameState.Idle           -> handleIdle(state)
                is GameState.AnswerCorrect  -> handleAnswerCorrect(state)
                is GameState.AnswerWrong    -> handleAnswerWrong()
                is GameState.SurahCompleted -> handleSurahCompleted(state)
            }
        }
    }

    private fun setupListeners() {
        binding.btnPeriksa.setOnClickListener { viewModel.checkAnswer() }
    }

    private fun loadCurrentAyat() {
        if (currentAyatIndex < ayatList.size) {
            viewModel.loadAyat(ayatList[currentAyatIndex])
        }
    }

    private fun handleIdle(state: GameState.Idle) {
        binding.tvAyatProgress.text               = "Ayat ${state.ayatNumber} dari ${state.totalAyat}"
        binding.cardFeedback.visibility           = View.INVISIBLE
        binding.btnPeriksa.visibility             = View.VISIBLE
        binding.btnPeriksa.isEnabled              = true
        binding.layoutCompletedButtons.visibility = View.GONE

        wordAdapter = WordAdapter(
            words = state.shuffledWords.toMutableList(),
            onDragStarted = { viewHolder -> itemTouchHelper.startDrag(viewHolder) }
        )
        val callback = DragDropCallback(
            adapter = wordAdapter,
            onMoveFinished = { from, to -> viewModel.onDragAndDrop(from, to) }
        )
        itemTouchHelper = ItemTouchHelper(callback)
        binding.rvWords.adapter = wordAdapter
        itemTouchHelper.attachToRecyclerView(binding.rvWords)
    }

    private fun handleAnswerCorrect(state: GameState.AnswerCorrect) {
        binding.cardFeedback.visibility = View.VISIBLE
        binding.cardFeedback.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
        binding.tvFeedback.text = "✓ Benar!   Skor: ${state.score}   Combo: x${state.combo}   Streak: ${state.currentStreak}"
        binding.tvScore.text    = state.score.toString()
        binding.tvCombo.text    = "x${state.combo}"
        binding.tvStreak.text   = state.currentStreak.toString()
        binding.btnPeriksa.isEnabled = false

        saveProgressMidGame(
            score        = state.score,
            bestStreak   = state.bestStreak,
            currentVerse = currentAyatIndex + 1
        )

        binding.root.postDelayed({
            currentAyatIndex++
            loadCurrentAyat()
        }, 1200)
    }

    private fun handleAnswerWrong() {
        binding.cardFeedback.visibility = View.VISIBLE
        binding.cardFeedback.setCardBackgroundColor(Color.parseColor("#FFDAD6"))
        binding.tvFeedback.text = "✗ Salah. Susun ulang dan coba lagi."
        binding.tvCombo.text    = "x0"
    }

    private fun handleSurahCompleted(state: GameState.SurahCompleted) {
        // Feedback card
        binding.cardFeedback.visibility = View.VISIBLE
        binding.cardFeedback.setCardBackgroundColor(Color.parseColor("#E8F5E9"))

        val currentOrder = HafalanConfig.getOrderByChapterId(chapterId)
        val nextSurah    = HafalanConfig.getByOrder(currentOrder + 1)
        val pesanUnlock  = if (nextSurah != null)
            "${nextSurah.chapterName} terbuka!"
        else
            "Selamat, seluruh hafalan telah selesai."

        binding.tvFeedback.text =
            "🎉 Surah Selesai!\nSkor: ${state.finalScore}   Best Streak: ${state.bestStreak}\n$pesanUnlock"

        // Swap tombol: sembunyikan Periksa, tampilkan dua tombol Completed
        binding.btnPeriksa.visibility           = View.GONE
        binding.layoutCompletedButtons.visibility = View.VISIBLE

        setupCompletedButtons(nextSurah)
        saveProgressOnComplete(state)
    }

    private fun setupCompletedButtons(nextSurah: com.example.sambungayat.ui.sambungayat.HafalanSurah?) {
        // Tombol Kembali ke Home — buka MainActivity, clear back stack, tab Home aktif
        binding.btnBackToHome.setOnClickListener {
            val intent = Intent(this, com.example.sambungayat.ui.main.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        // Tombol Lanjutkan
        if (nextSurah != null) {
            binding.btnLanjutkan.isEnabled = true
            binding.btnLanjutkan.setOnClickListener {
                startActivity(
                    Intent(this, GameActivity::class.java).apply {
                        putExtra(EXTRA_CHAPTER_ID,   nextSurah.chapterId)
                        putExtra(EXTRA_CHAPTER_NAME, nextSurah.chapterName)
                        putExtra(EXTRA_VERSE_COUNT,  0)
                    }
                )
                finish()
            }
        } else {
            // Tidak ada surah berikutnya — nonaktifkan Lanjutkan
            binding.btnLanjutkan.isEnabled = false
            binding.btnLanjutkan.text      = "Selesai"
        }
    }

    // ------------------------------------------------------------------
    // SAVE PROGRESS — highestUnlockedSurah = posisi ORDER, bukan chapterId
    // ------------------------------------------------------------------

    private fun saveProgressMidGame(score: Int, bestStreak: Int, currentVerse: Int) {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        // highest_unlocked_surah tidak berubah saat mid-game
        val currentHighestOrder = sessionManager.getHighestSurah().coerceAtLeast(1)

        sessionManager.saveProgress(
            totalScore           = score,
            bestStreak           = bestStreak,
            currentSurah         = chapterId,
            currentVerse         = currentVerse,
            highestUnlockedSurah = currentHighestOrder
        )

        lifecycleScope.launch {
            userRepository.saveProgress(
                userId               = userId,
                totalScore           = score,
                bestStreak           = bestStreak,
                highestUnlockedSurah = currentHighestOrder,
                currentSurah         = chapterId,
                currentVerse         = currentVerse
            )
        }
    }

    private fun saveProgressOnComplete(state: GameState.SurahCompleted) {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        // Hitung nextOrder berdasarkan posisi surah ini dalam HAFALAN_LIST
        val currentOrder     = HafalanConfig.getOrderByChapterId(chapterId)
        val nextOrder        = if (currentOrder != -1) currentOrder + 1 else sessionManager.getHighestSurah()
        val newHighestOrder  = maxOf(sessionManager.getHighestSurah(), nextOrder)
            .coerceAtMost(HafalanConfig.TOTAL + 1)  // cap agar tidak melebihi total

        // currentSurah tetap menyimpan chapterId untuk kebutuhan Continue Learning
        val nextChapterId    = HafalanConfig.getChapterIdByOrder(nextOrder)
            .takeIf { it != -1 } ?: chapterId

        sessionManager.saveProgress(
            totalScore           = state.finalScore,
            bestStreak           = state.bestStreak,
            currentSurah         = nextChapterId,
            currentVerse         = 1,
            highestUnlockedSurah = newHighestOrder   // ← ini sekarang adalah ORDER, bukan chapterId
        )

        lifecycleScope.launch {
            userRepository.saveProgress(
                userId               = userId,
                totalScore           = state.finalScore,
                bestStreak           = state.bestStreak,
                highestUnlockedSurah = newHighestOrder,
                currentSurah         = nextChapterId,
                currentVerse         = 1
            )
        }
    }

    private fun setLoadingState(loading: Boolean) {
        binding.rvWords.visibility                = if (loading) View.GONE else View.VISIBLE
        binding.btnPeriksa.visibility             = View.VISIBLE
        binding.btnPeriksa.isEnabled              = !loading
        binding.layoutCompletedButtons.visibility = View.GONE
    }
}