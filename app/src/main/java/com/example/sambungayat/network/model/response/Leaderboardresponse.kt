package com.example.sambungayat.network.model.response

import com.google.gson.annotations.SerializedName

data class LeaderboardResponse(
    @SerializedName("rank")        val rank: Int,
    @SerializedName("name")        val name: String,
    @SerializedName("total_score") val totalScore: Int
)