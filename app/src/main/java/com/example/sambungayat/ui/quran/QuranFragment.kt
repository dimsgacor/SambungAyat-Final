package com.example.sambungayat.ui.quran

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.sambungayat.databinding.FragmentQuranBinding
import com.example.sambungayat.network.ApiResult
import com.example.sambungayat.network.model.response.JuzResponse
import com.example.sambungayat.network.repository.QuranRepository
import kotlinx.coroutines.launch

class QuranFragment : Fragment() {

    private var _binding: FragmentQuranBinding? = null
    private val binding get() = _binding!!

    private val quranRepository = QuranRepository()

    // Simpan full list untuk filter lokal
    private var allJuzList: List<JuzResponse> = emptyList()
    private lateinit var juzAdapter: JuzAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvJuz.layoutManager = GridLayoutManager(requireContext(), 2)
        setupSearch()
        loadJuz()
    }

    private fun loadJuz() {
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = quranRepository.getJuz()) {
                is ApiResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    allJuzList = result.data
                    showJuzList(allJuzList)
                }
                is ApiResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                }
                else -> {}
            }
        }
    }

    private fun showJuzList(list: List<JuzResponse>) {
        juzAdapter = JuzAdapter(list) { juz ->
            val intent = Intent(requireContext(), ChapterListActivity::class.java).apply {
                putExtra("juz_id",     juz.id)
                putExtra("juz_number", juz.number)
                putExtra("juz_name",   juz.name)
            }
            startActivity(intent)
        }
        binding.rvJuz.adapter = juzAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterJuz(s?.toString()?.trim() ?: "")
            }
        })
    }

    private fun filterJuz(query: String) {
        if (allJuzList.isEmpty()) return

        val filtered = if (query.isBlank()) {
            allJuzList
        } else {
            allJuzList.filter { juz ->
                juz.name.contains(query, ignoreCase = true) ||
                juz.number.toString().contains(query)
            }
        }
        showJuzList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
