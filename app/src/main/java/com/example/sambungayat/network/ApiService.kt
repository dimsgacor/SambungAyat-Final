package com.example.sambungayat.network

import com.example.sambungayat.network.model.request.LoginRequest
import com.example.sambungayat.network.model.request.RegisterRequest
import com.example.sambungayat.network.model.request.SaveProgressRequest
import com.example.sambungayat.network.model.response.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("login.php")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("register.php")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @GET("get_profile.php")
    suspend fun getProfile(@Query("user_id") userId: Int): Response<ProfileResponse>

    @GET("get_juz.php")
    suspend fun getJuz(): Response<List<JuzResponse>>

    @GET("get_chapters.php")
    suspend fun getChapters(): Response<List<ChapterResponse>>

    @GET("get_chapters_by_juz.php")
    suspend fun getChaptersByJuz(@Query("juz") juz: Int): Response<List<ChapterResponse>>

    @GET("get_surah_detail.php")
    suspend fun getSurahDetail(@Query("chapter_id") chapterId: Int): Response<SurahDetailResponse>

    @GET("get_verses.php")
    suspend fun getVerses(@Query("chapter_id") chapterId: Int): Response<List<VerseResponse>>

    @GET("get_progress.php")
    suspend fun getProgress(@Query("user_id") userId: Int): Response<ProgressResponse>

    @POST("save_progress.php")
    suspend fun saveProgress(@Body body: SaveProgressRequest): Response<SaveProgressResponse>

    @GET("get_statistics.php")
    suspend fun getStatistics(@Query("user_id") userId: Int): Response<StatisticsResponse>

    @GET("get_leaderboard.php")
    suspend fun getLeaderboard(@Query("limit") limit: Int = 5): Response<List<LeaderboardResponse>>
}