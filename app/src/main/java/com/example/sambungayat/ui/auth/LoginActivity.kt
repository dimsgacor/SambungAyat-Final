package com.example.sambungayat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.sambungayat.databinding.ActivityLoginBinding
import com.example.sambungayat.network.ApiResult
import com.example.sambungayat.network.SessionManager
import com.example.sambungayat.network.repository.AuthRepository
import com.example.sambungayat.network.repository.UserRepository
import com.example.sambungayat.ui.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            goToMain()
            return
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email    = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        var valid = true
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email tidak boleh kosong"; valid = false
        } else binding.tilEmail.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"; valid = false
        } else binding.tilPassword.error = null

        if (!valid) return
        setLoading(true)

        lifecycleScope.launch {
            when (val result = authRepository.login(email, password)) {
                is ApiResult.Success -> {
                    val data = result.data
                    val userId   = data.userId ?: 0
                    val username = data.username ?: ""
                    sessionManager.saveSession(userId, username)

                    // Langsung sync progress dari backend ke cache lokal
                    syncProgressFromBackend(userId)
                }
                is ApiResult.Error -> {
                    setLoading(false)
                    Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_SHORT).show()
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    private suspend fun syncProgressFromBackend(userId: Int) {
        when (val result = userRepository.getProgress(userId)) {
            is ApiResult.Success -> {
                val d = result.data
                sessionManager.saveProgress(
                    totalScore           = d.totalScore,
                    bestStreak           = d.bestStreak,
                    currentSurah         = d.currentSurah,
                    currentVerse         = d.currentVerse,
                    highestUnlockedSurah = d.highestUnlockedSurah
                )
            }
            else -> {
                // Tidak ada progress sebelumnya — biarkan default (0, surah 1, ayat 1)
            }
        }
        setLoading(false)
        goToMain()
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled     = !loading
    }
}
