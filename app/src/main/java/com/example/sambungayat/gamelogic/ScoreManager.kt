package com.example.sambungayat.gamelogic

class ScoreManager {

    companion object {
        const val POINTS_PER_CORRECT = 10
    }

    private var score: Int = 0

    fun onCorrectAnswer() { score += POINTS_PER_CORRECT }
    fun onWrongAnswer()   { /* skor tidak berubah jika salah */ }

    fun getScore(): Int = score

    /** Inject nilai awal dari backend agar tidak reset antar surah. */
    fun setScore(value: Int) { score = value }

    fun reset() { score = 0 }
}
