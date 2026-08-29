package com.example.syllabusmarksheetandattendanceregister.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.syllabusmarksheetandattendanceregister.R
import com.example.syllabusmarksheetandattendanceregister.databinding.ItemStudentBinding
import com.example.syllabusmarksheetandattendanceregister.datamodels.Student

class StudentsAdapter(
    private val studentClickListener: ItemClickLister,
    private val studentRegListener: SwitchStateChangeListener
) : RecyclerView.Adapter<StudentsAdapter.StudentViewHolder>() {

    private lateinit var students: List<Student>

    class StudentViewHolder(val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        val context = holder.itemView.context
        
        holder.binding.textStudentName.text = student.name
        holder.binding.textStudentName.isEnabled = student.isRegistered

        holder.binding.textRegId.text = context.getString(R.string.student_reg_id_format, student.matricule)
        holder.binding.textGender.text = context.getString(R.string.student_gender_format, student.gender)

        holder.binding.textScore.text = context.getString(R.string.student_score_format, student.score.toString())
        holder.binding.textScore.isEnabled = student.isRegistered
        
        val scoreColor = if (student.score in 10.0..20.0) {
            context.getColor(R.color.colorPrimary)
        } else {
            context.getColor(R.color.error)
        }
        holder.binding.textScore.setTextColor(scoreColor)
        
        holder.binding.switchRegistered.setOnCheckedChangeListener(null)
        holder.binding.switchRegistered.isChecked = student.isRegistered

        holder.binding.switchRegistered.setOnCheckedChangeListener { _, isChecked ->
            studentRegListener.onSwitchStateChange(position, isChecked)
        }

        holder.itemView.setOnClickListener {
            studentClickListener.onItemClick(position, student)
        }
    }

    override fun getItemCount(): Int = students.size

    fun updateData(newStudents: List<Student>) {
        students = newStudents
        notifyDataSetChanged()

    }

    interface SwitchStateChangeListener{
        fun onSwitchStateChange(studentIndex: Int, state: Boolean)
    }

    interface ItemClickLister{
        fun onItemClick(position: Int, student: Student)
    }
}
