package com.example.syllabusmarksheetandattendanceregister.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.syllabusmarksheetandattendanceregister.R
import com.example.syllabusmarksheetandattendanceregister.databinding.ItemChapterBinding
import com.example.syllabusmarksheetandattendanceregister.datamodels.ChapterData

class ChaptersAdapter(
    private var chapters: List<ChapterData>,
    private var listener: ChapterClickListener
) : RecyclerView.Adapter<ChaptersAdapter.ChapterViewHolder>() {

    class ChapterViewHolder(val binding: ItemChapterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemChapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val chapter = chapters[position]
        holder.binding.textChapterName.text = chapter.chapter
        val totalLessons = chapter.lessons.size
        val lessonsDone = chapter.lessons.count { it.isTaught }
        holder.binding.textLessonStats.text = holder.itemView.context.getString(R.string.lesson_stats_format, lessonsDone, totalLessons)
        holder.binding.progressChapter.max = totalLessons
        holder.binding.progressChapter.progress = lessonsDone

        holder.itemView.setOnClickListener { listener.onChapterClick(position, chapters[position]) }
    }

    override fun getItemCount(): Int = chapters.size

    fun updateChapters(newChapters: List<ChapterData>) {
        chapters = newChapters
        notifyDataSetChanged()
    }
}

interface ChapterClickListener {
    fun onChapterClick(chapterIndex: Int, chapter: ChapterData)
}