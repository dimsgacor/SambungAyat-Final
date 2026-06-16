package com.example.sambungayat.gamelogic

class StreakManager {

    private var currentStreak: Int = 0
    private var bestStreak: Int    = 0

    fun onCorrectAnswer() {
        currentStreak++
        if (currentStreak > bestStreak) bestStreak = currentStreak
    }

    fun onWrongAnswer() {
        currentStreak = 0
    }

    fun getCurrentStreak(): Int = currentStreak
    fun getBestStreak(): Int    = bestStreak

    /** Inject nilai bestStreak dari backend agar tidak reset antar surah. */
    fun setBestStreak(value: Int) { bestStreak = value }

    fun reset() {
        currentStreak = 0
        bestStreak    = 0
    }
}
