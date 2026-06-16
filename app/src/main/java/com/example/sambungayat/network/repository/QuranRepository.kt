package com.example.sambungayat.network.repository

import com.example.sambungayat.network.ApiClient
import com.example.sambungayat.network.ApiResult
import com.example.sambungayat.network.model.response.*

class QuranRepository {

    private val api = ApiClient.apiService

    suspend fun getJuz(): ApiResult<List<JuzResponse>> {
        return try {
            val response = api.getJuz()
            if (response.isSuccessful) ApiResult.Success(response.body() ?: emptyList())
            else ApiResult.Error("Error ${response.code()}")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }

    suspend fun getChapters(): ApiResult<List<ChapterResponse>> {
        return try {
            val response = api.getChapters()
            if (response.isSuccessful) ApiResult.Success(response.body() ?: emptyList())
            else ApiResult.Error("Error ${response.code()}")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }

    suspend fun getChaptersByJuz(juz: Int): ApiResult<List<ChapterResponse>> {
        return try {
            val response = api.getChaptersByJuz(juz)
            if (response.isSuccessful) ApiResult.Success(response.body() ?: emptyList())
            else ApiResult.Error("Error ${response.code()}")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }

    suspend fun getSurahDetail(chapterId: Int): ApiResult<SurahDetailResponse> {
        return try {
            val response = api.getSurahDetail(chapterId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Error("Data surah kosong")
            } else ApiResult.Error("Error ${response.code()}")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }

    suspend fun getVerses(chapterId: Int): ApiResult<List<VerseResponse>> {
        return try {
            val response = api.getVerses(chapterId)
            if (response.isSuccessful) ApiResult.Success(response.body() ?: emptyList())
            else ApiResult.Error("Error ${response.code()}")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }

    suspend fun getLeaderboard(limit: Int = 5): ApiResult<List<LeaderboardResponse>> {
        return try {
            val response = api.getLeaderboard(limit)
            if (response.isSuccessful) ApiResult.Success(response.body() ?: emptyList())
            else ApiResult.Error("Error ${response.code()}")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Tidak dapat terhubung ke server")
        }
    }
}