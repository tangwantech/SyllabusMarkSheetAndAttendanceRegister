package com.example.syllabusmarksheetandattendanceregister.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.syllabusmarksheetandattendanceregister.R
import com.example.syllabusmarksheetandattendanceregister.databinding.ItemAttendanceRecordBinding
import com.example.syllabusmarksheetandattendanceregister.datamodels.Attendance
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceRecordsAdapter : RecyclerView.Adapter<AttendanceRecordsAdapter.ViewHolder>() {

    private var records: List<Pair<String, Attendance>> = emptyList()

    private val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val outputFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.US)

    class ViewHolder(val binding: ItemAttendanceRecordBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (dateStr, attendance) = records[position]
        val context = holder.itemView.context

        holder.binding.textRecordDate.text = try {
            val date = inputFormat.parse(dateStr)
            if (date != null) outputFormat.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
        
        if (attendance.isPresent == true) {
            holder.binding.textRecordStatus.text = context.getString(R.string.label_present)
            holder.binding.textRecordStatus.setTextColor(context.getColor(R.color.colorPrimary))
            holder.binding.textRecordAbsenceCount.text = context.getString(R.string.absences_count_format, 0)
        } else {
            holder.binding.textRecordStatus.text = context.getString(R.string.label_absent)
            holder.binding.textRecordStatus.setTextColor(context.getColor(R.color.error))
            holder.binding.textRecordAbsenceCount.text = context.getString(R.string.absences_count_format, attendance.absenceCount ?: 0)
        }
    }

    override fun getItemCount(): Int = records.size

    fun updateRecords(newRecords: HashMap<String, Attendance>?) {
        records = newRecords?.entries?.sortedByDescending { it.key }?.map { it.toPair() } ?: emptyList()
        notifyDataSetChanged()
    }
}
