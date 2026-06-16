package com.example.sambungayat.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sambungayat.databinding.FragmentProfileBinding
import com.example.sambungayat.network.ApiResult
import com.example.sambungayat.network.SessionManager
import com.example.sambungayat.network.repository.UserRepository
import com.example.sambungayat.ui.auth.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val userRepository = UserRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        loadData()
        setupListeners()
    }

    private fun loadData() {
        val userId = sessionManager.getUserId()

        // Tampilkan cache lokal dulu sementara menunggu API
        binding.tvProfileName.text   = sessionManager.getUsername()
        binding.tvProfileScore.text  = sessionManager.getTotalScore().toString()
        binding.tvProfileStreak.text = sessionManager.getBestStreak().toString()

        viewLifecycleOwner.lifecycleScope.launch {

            // Profile — nama + email
            when (val result = userRepository.getProfile(userId)) {
                is ApiResult.Success -> {
                    binding.tvProfileName.text  = result.data.name
                    binding.tvProfileEmail.text = result.data.email
                }
                else -> {}
            }

            // Statistics — score + streak + progress
            when (val result = userRepository.getStatistics(userId)) {
                is ApiResult.Success -> {
                    val d = result.data
                    binding.tvProfileScore.text  = d.totalScore.toString()
                    // tvProfileStreak menampilkan bestStreak (pencapaian tertinggi)
                    binding.tvProfileStreak.text = d.bestStreak.toString()

                    val pct = ((d.highestUnlockedSurah.toFloat() / 114f) * 100).toInt()
                    binding.progressQuran.progress  = pct
                    binding.tvProgressPercent.text  = "$pct% Completed"
                    binding.tvProgressDetail.text   =
                        "You've unlocked ${d.highestUnlockedSurah} of 114 Surahs. Keep going!"

                    // Sync cache lokal
                    sessionManager.saveProgress(
                        totalScore           = d.totalScore,
                        bestStreak           = d.bestStreak,
                        currentSurah         = d.currentSurah,
                        currentVerse         = sessionManager.getCurrentVerse(),
                        highestUnlockedSurah = d.highestUnlockedSurah
                    )
                }
                else -> {}
            }
        }
    }

    private fun setupListeners() {
        binding.cardLogout.setOnClickListener { confirmLogout() }
        binding.cardEditProfile.setOnClickListener { /* TODO: Edit Profile */ }
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Yakin ingin keluar?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Logout") { _, _ ->
                sessionManager.clearSession()
                startActivity(
                    Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
