package com.example.syllabusmarksheetandattendanceregister.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.syllabusmarksheetandattendanceregister.R
import com.example.syllabusmarksheetandattendanceregister.databinding.ItemStudentDatabaseBinding
import com.example.syllabusmarksheetandattendanceregister.datamodels.StudentData

class StudentsDatabaseAdapter(
    private val isDeleteMode: Boolean = false,
    private val onLongClickListener: ((Int) -> Unit)? = null,
    private val onCheckedChangeListener: ((Int, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<StudentsDatabaseAdapter.ViewHolder>() {

    private var students: List<StudentData> = emptyList()
    private var selectedIndices: Set<Int> = emptySet()

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

        if (isDeleteMode) {
            holder.binding.checkboxDelete.visibility = View.VISIBLE
            holder.binding.checkboxDelete.setOnCheckedChangeListener(null)
            holder.binding.checkboxDelete.isChecked = selectedIndices.contains(position)
            holder.binding.checkboxDelete.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChangeListener?.invoke(position, isChecked)
            }
        } else {
            holder.binding.checkboxDelete.visibility = View.GONE
            holder.itemView.setOnLongClickListener {
                onLongClickListener?.invoke(position)
                true
            }
        }
    }

    override fun getItemCount(): Int = students.size

    fun updateData(newStudents: List<StudentData>, newSelectedIndices: Set<Int> = emptySet()) {
        students = newStudents
        selectedIndices = newSelectedIndices
        notifyDataSetChanged()
    }
}