package com.example.sambungayat.gamelogic

class GameManager(
    private val validationManager: ValidationManager,
    private val scoreManager: ScoreManager,
    private val comboManager: ComboManager,
    private val streakManager: StreakManager,
    private val surahProgressManager: SurahProgressManager
) {

    private var correctOrder: List<String> = emptyList()
    private var totalSurahInApp: Int = 114

    fun setTotalSurahInApp(total: Int) {
        totalSurahInApp = total
    }

    fun startSurah(surahId: Int, totalAyat: Int) {
        // BUG FIX #1: Hapus scoreManager.reset() dari sini.
        // Score adalah nilai kumulatif global yang di-inject dari backend via initFromBackend().
        // Me-reset score di sini membuang nilai yang baru saja di-inject,
        // sehingga score selalu mulai dari 0 setiap surah baru.
        // Combo dan streak boleh di-reset karena memang per-surah.
        comboManager.reset()
        streakManager.reset()
        surahProgressManager.startSurah(surahId, totalAyat)
    }

    fun loadAyat(ayatText: String): GameState.Idle {
        correctOrder = validationManager.splitAyat(ayatText)
        val shuffledWords = validationManager.shuffle(correctOrder)

        return GameState.Idle(
            shuffledWords = shuffledWords,
            ayatNumber = surahProgressManager.getCurrentAyatNumber(),
            totalAyat = surahProgressManager.getTotalAyat()
        )
    }

    fun onDragAndDrop(userOrder: MutableList<String>, fromIndex: Int, toIndex: Int) {
        if (fromIndex < 0 || toIndex < 0) return
        if (fromIndex >= userOrder.size || toIndex >= userOrder.size) return
        if (fromIndex == toIndex) return

        val word = userOrder.removeAt(fromIndex)
        userOrder.add(toIndex, word)
    }

    fun checkAnswer(userOrder: List<String>): GameState {
        val isCorrect = validationManager.validate(correctOrder, userOrder)
        return if (isCorrect) handleCorrectAnswer() else handleWrongAnswer()
    }

    private fun handleCorrectAnswer(): GameState {
        scoreManager.onCorrectAnswer()
        comboManager.onCorrectAnswer()
        streakManager.onCorrectAnswer()
        surahProgressManager.onAyatCompleted()

        return if (surahProgressManager.isSurahCompleted()) {
            val nextSurahId = surahProgressManager.unlockNextSurah(totalSurahInApp)
            GameState.SurahCompleted(
                surahId = surahProgressManager.getCurrentSurahId(),
                finalScore = scoreManager.getScore(),
                bestStreak = streakManager.getBestStreak(),
                unlockedNextSurah = nextSurahId
            )
        } else {
            GameState.AnswerCorrect(
                score = scoreManager.getScore(),
                combo = comboManager.getCombo(),
                currentStreak = streakManager.getCurrentStreak(),
                bestStreak = streakManager.getBestStreak()
            )
        }
    }

    private fun handleWrongAnswer(): GameState {
        scoreManager.onWrongAnswer()
        comboManager.onWrongAnswer()
        streakManager.onWrongAnswer()
        return GameState.AnswerWrong
    }

    fun getCurrentScore(): Int = scoreManager.getScore()

    fun getCurrentCombo(): Int = comboManager.getCombo()

    fun isSurahUnlocked(surahId: Int): Boolean = surahProgressManager.isSurahUnlocked(surahId)

    fun getUnlockedSurahList(): List<Int> = surahProgressManager.getUnlockedSurahList()
}