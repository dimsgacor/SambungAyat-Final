package com.example.sambungayat.ui.sambungayat

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sambungayat.databinding.ItemSurahProgressBinding
import com.example.sambungayat.ui.game.GameActivity

class SurahProgressAdapter(
    private val items        : List<HafalanSurah>,
    private val highestOrder : Int,     // posisi order tertinggi yang sudah terbuka
    private val verseCountMap: Map<Int, Int>  // chapterId → verseCount dari API
) : RecyclerView.Adapter<SurahProgressAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSurahProgressBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSurahProgressBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val surah = items[position]
        holder.binding.tvSurahName.text  = surah.chapterName
        holder.binding.tvVerseCount.text =
            "${verseCountMap[surah.chapterId] ?: "?"} ayat"

        when {
            surah.order < highestOrder -> bindDone(holder)
            surah.order == highestOrder -> bindInProgress(holder, surah)
            else -> bindLocked(holder)
        }
    }

    private fun bindDone(holder: ViewHolder) {
        holder.binding.tvStatusLabel.text   = "✅ Lulus"
        holder.binding.tvProgressLabel.text = "100% Completed"
        holder.binding.progressBar.progress = 100
        holder.binding.root.alpha           = 1f
        holder.binding.root.setOnClickListener(null)
        holder.binding.root.isClickable = false
    }

    private fun bindInProgress(holder: ViewHolder, surah: HafalanSurah) {
        holder.binding.tvStatusLabel.text   = "▶ Lanjutkan"
        holder.binding.tvProgressLabel.text = "Sedang Dikerjakan"
        holder.binding.progressBar.progress = 40
        holder.binding.root.alpha           = 1f
        holder.binding.root.isClickable     = true
        holder.binding.root.setOnClickListener {
            val ctx = holder.binding.root.context
            ctx.startActivity(
                Intent(ctx, GameActivity::class.java).apply {
                    putExtra(GameActivity.EXTRA_CHAPTER_ID,   surah.chapterId)
                    putExtra(GameActivity.EXTRA_CHAPTER_NAME, surah.chapterName)
                    putExtra(GameActivity.EXTRA_VERSE_COUNT,  verseCountMap[surah.chapterId] ?: 0)
                }
            )
        }
    }

    private fun bindLocked(holder: ViewHolder) {
        holder.binding.tvStatusLabel.text   = "🔒 Terkunci"
        holder.binding.tvProgressLabel.text = "Selesaikan surah sebelumnya"
        holder.binding.progressBar.progress = 0
        holder.binding.root.alpha           = 0.5f
        holder.binding.root.isClickable     = false
        holder.binding.root.setOnClickListener(null)
    }

    override fun getItemCount() = items.size
}
