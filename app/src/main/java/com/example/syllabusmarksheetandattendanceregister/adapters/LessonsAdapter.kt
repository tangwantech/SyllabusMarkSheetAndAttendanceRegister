package com.example.syllabusmarksheetandattendanceregister.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.syllabusmarksheetandattendanceregister.databinding.ItemLessonBinding
import com.example.syllabusmarksheetandattendanceregister.datamodels.Lesson

class LessonsAdapter(
    private val listener: LessonCheckChangeListener
) : RecyclerView.Adapter<LessonsAdapter.LessonViewHolder>() {
    private lateinit var lessons: List<Lesson>

    class LessonViewHolder(val binding: ItemLessonBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
        val binding = ItemLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LessonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
        val lesson = lessons[position]
        holder.binding.textLessonName.text = lesson.lesson
        
        // Remove listener before setting checked to avoid circular trigger
        holder.binding.checkboxLesson.setOnCheckedChangeListener(null)
        holder.binding.checkboxLesson.isChecked = lesson.isTaught
        
        holder.binding.checkboxLesson.setOnCheckedChangeListener { _, isChecked ->
            listener.onLessonCheckChange(position, isChecked)
        }

        holder.binding.root.setOnClickListener {
            holder.binding.checkboxLesson.isChecked  = !holder.binding.checkboxLesson.isChecked
//            listener.onLessonCheckChange(position, isChecked)
        }
    }

    override fun getItemCount(): Int = lessons.size

    fun updateData(newLessons: List<Lesson>) {
        lessons = newLessons
        notifyDataSetChanged()
    }

}

interface LessonCheckChangeListener {
    fun onLessonCheckChange(lessonIndex: Int, isChecked: Boolean)
}