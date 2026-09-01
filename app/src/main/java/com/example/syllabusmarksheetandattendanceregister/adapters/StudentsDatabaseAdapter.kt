package com.example.syllabusmarksheetandattendanceregister.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.syllabusmarksheetandattendanceregister.R
import com.example.syllabusmarksheetandattendanceregister.databinding.ItemStudentDatabaseBinding
import com.example.syllabusmarksheetandattendanceregister.datamodels.StudentData

class StudentsDatabaseAdapter : RecyclerView.Adapter<StudentsDatabaseAdapter.ViewHolder>() {

    private var students: List<StudentData> = emptyList()

    class ViewHolder(val binding: ItemStudentDatabaseBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentDatabaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        val context = holder.itemView.context

        holder.binding.textStudentName.text = student.name
        holder.binding.textRegId.text = context.getString(R.string.student_reg_id_format, student.matricule)
        holder.binding.textGender.text = context.getString(R.string.student_gender_format, student.gender)
    }

    override fun getItemCount(): Int = students.size

    fun updateData(newStudents: List<StudentData>) {
        students = newStudents
        notifyDataSetChanged()
    }
}