package com.example.sambungayat.network.model.response

import com.google.gson.annotations.SerializedName

data class StatisticsResponse(
    @SerializedName("total_score")            val totalScore: Int,
    @SerializedName("best_streak")            val bestStreak: Int,
    @SerializedName("current_streak")         val currentStreak: Int,
    @SerializedName("current_surah")          val currentSurah: Int,
    @SerializedName("highest_unlocked_surah") val highestUnlockedSurah: Int
)
