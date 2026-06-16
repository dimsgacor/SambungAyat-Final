package com.example.sambungayat.gamelogic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {

    private val validationManager    = ValidationManager()
    private val scoreManager         = ScoreManager()
    private val comboManager         = ComboManager()
    private val streakManager        = StreakManager()
    private val surahProgressManager = SurahProgressManager()

    private val gameManager = GameManager(
        validationManager,
        scoreManager,
        comboManager,
        streakManager,
        surahProgressManager
    )

    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState

    private val _userOrder = MutableLiveData<MutableList<String>>()
    val userOrder: LiveData<MutableList<String>> = _userOrder

    fun setTotalSurahInApp(total: Int) {
        gameManager.setTotalSurahInApp(total)
    }

    /**
     * Inject score dan streak global dari backend agar tidak reset antar surah.
     * Dipanggil sekali sebelum startSurah(), setelah getProgress() berhasil.
     */
    fun initFromBackend(totalScore: Int, bestStreak: Int) {
        scoreManager.setScore(totalScore)
        streakManager.setBestStreak(bestStreak)
    }

    fun startSurah(surahId: Int, totalAyat: Int) {
        gameManager.startSurah(surahId, totalAyat)
    }

    fun loadAyat(ayatText: String) {
        val idleState = gameManager.loadAyat(ayatText)
        _userOrder.value = idleState.shuffledWords.toMutableList()
        _gameState.value = idleState
    }

    fun onDragAndDrop(fromIndex: Int, toIndex: Int) {
        val currentList = _userOrder.value ?: return
        gameManager.onDragAndDrop(currentList, fromIndex, toIndex)
        _userOrder.value = currentList
    }

    fun checkAnswer() {
        val currentUserOrder = _userOrder.value ?: return
        val result = gameManager.checkAnswer(currentUserOrder)
        _gameState.value = result
    }

    fun loadNextAyat(nextAyatText: String) = loadAyat(nextAyatText)

    fun isSurahUnlocked(surahId: Int): Boolean = gameManager.isSurahUnlocked(surahId)
    fun getUnlockedSurahList(): List<Int>       = gameManager.getUnlockedSurahList()
    fun getCurrentScore(): Int                  = gameManager.getCurrentScore()
    fun getCurrentCombo(): Int                  = gameManager.getCurrentCombo()
}
