package com.example.syllabusmarksheetandattendanceregister.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.syllabusmarksheetandattendanceregister.R
import com.example.syllabusmarksheetandattendanceregister.databinding.ItemStudentAttendanceBinding
import com.example.syllabusmarksheetandattendanceregister.datamodels.Attendance
import com.example.syllabusmarksheetandattendanceregister.datamodels.StudentAttendanceData
import kotlin.toString

class StudentsAttendanceAdapter(
    private val selectedDate: String,
    private val listener: AttendanceChangeListener,
    private val clickListener: ItemClickListener
) : RecyclerView.Adapter<StudentsAttendanceAdapter.ViewHolder>() {

    private var students: List<StudentAttendanceData> = emptyList()
    private var currentAbsenceWeight: Int = 0

    class ViewHolder(val binding: ItemStudentAttendanceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val student = students[position]
        val context = holder.itemView.context

        holder.binding.textStudentName.text = student.name
        holder.binding.textMatricule.text = context.getString(R.string.student_reg_id_format, student.matricule)
        holder.binding.textGender.text = context.getString(R.string.student_gender_format, student.gender)
        
        // Get current status for today if it exists
        val todayAttendance = student.attendances?.get(selectedDate)
        
        // Calculate cumulative absences from the past (excluding today)
        val pastAbsences = student.attendances?.entries
            ?.filter { it.key != selectedDate && it.value.isPresent == false }
            ?.sumOf { it.value.absenceCount ?: 0 } ?: 0

        // Logic: default is 0. If absent, use currentAbsenceWeight.
        val todayAbsenceDisplay = if (todayAttendance?.isPresent == false) currentAbsenceWeight else 0
        
        holder.binding.textAbsencesToday.text = "Today\'s Absences: $todayAbsenceDisplay"
        holder.binding.textCumulatedAbsences.text = "Cumulated: ${pastAbsences + todayAbsenceDisplay}"

        // Remove listener before setting state to avoid triggering it during binding
        holder.binding.radioGroup.setOnCheckedChangeListener(null)

        // Handle status display and radio button state
        when (todayAttendance?.isPresent) {
            true -> {
                holder.binding.textStatus.text = "PRESENT"
                holder.binding.textStatus.setTextColor(context.getColor(R.color.colorPrimary))
                holder.binding.radioGroup.check(R.id.radioBtnPresent)
            }
            false -> {
                holder.binding.textStatus.text = "ABSENT"
                holder.binding.textStatus.setTextColor(context.getColor(R.color.error))
                holder.binding.radioGroup.check(R.id.radioBtnAbsent)
            }
            else -> {
                holder.binding.textStatus.text = ""
                holder.binding.radioGroup.clearCheck()
            }
        }

        holder.binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val isPresent = checkedId == R.id.radioBtnPresent
            val updatedTodayAbsence = if (isPresent) 0 else currentAbsenceWeight
            
            holder.binding.textAbsencesToday.text = "Today's Absences: $updatedTodayAbsence"
            holder.binding.textCumulatedAbsences.text = "Cumulated: ${pastAbsences + updatedTodayAbsence}"
            
            if (isPresent) {
                holder.binding.textStatus.text = "PRESENT"
                holder.binding.textStatus.setTextColor(context.getColor(R.color.colorPrimary))
            } else {
                holder.binding.textStatus.text = "ABSENT"
                holder.binding.textStatus.setTextColor(context.getColor(R.color.error))
            }

            listener.onAttendanceChanged(position, isPresent)
        }

        holder.itemView.setOnClickListener {
            clickListener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int = students.size

    fun updateData(newStudents: List<StudentAttendanceData>, absenceWeight: Int) {
        students = newStudents
        currentAbsenceWeight = absenceWeight
        notifyDataSetChanged()
    }

    interface AttendanceChangeListener {
        fun onAttendanceChanged(position: Int, isPresent: Boolean)
    }

    interface ItemClickListener {
        fun onItemClick(position: Int)
    }
}
