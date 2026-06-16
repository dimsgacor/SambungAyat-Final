package com.example.sambungayat.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    companion object {
        private const val PREF_NAME           = "sambung_ayat_session"
        private const val KEY_USER_ID         = "user_id"
        private const val KEY_USERNAME        = "username"
        private const val KEY_IS_LOGGED_IN    = "is_logged_in"
        private const val KEY_TOTAL_SCORE     = "total_score"
        private const val KEY_BEST_STREAK     = "best_streak"
        private const val KEY_CURRENT_SURAH   = "current_surah"
        private const val KEY_CURRENT_VERSE   = "current_verse"
        private const val KEY_HIGHEST_SURAH   = "highest_unlocked_surah"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveSession(userId: Int, username: String) {
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun saveProgress(
        totalScore: Int,
        bestStreak: Int,
        currentSurah: Int,
        currentVerse: Int,
        highestUnlockedSurah: Int
    ) {
        prefs.edit()
            .putInt(KEY_TOTAL_SCORE,   totalScore)
            .putInt(KEY_BEST_STREAK,   bestStreak)
            .putInt(KEY_CURRENT_SURAH, currentSurah)
            .putInt(KEY_CURRENT_VERSE, currentVerse)
            .putInt(KEY_HIGHEST_SURAH, highestUnlockedSurah)
            .apply()
    }

    fun getUserId(): Int         = prefs.getInt(KEY_USER_ID, -1)
    fun getUsername(): String    = prefs.getString(KEY_USERNAME, "") ?: ""
    fun isLoggedIn(): Boolean    = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun getTotalScore(): Int     = prefs.getInt(KEY_TOTAL_SCORE, 0)
    fun getBestStreak(): Int     = prefs.getInt(KEY_BEST_STREAK, 0)
    fun getCurrentSurah(): Int   = prefs.getInt(KEY_CURRENT_SURAH, 1)
    fun getCurrentVerse(): Int   = prefs.getInt(KEY_CURRENT_VERSE, 1)
    fun getHighestSurah(): Int   = prefs.getInt(KEY_HIGHEST_SURAH, 1)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
