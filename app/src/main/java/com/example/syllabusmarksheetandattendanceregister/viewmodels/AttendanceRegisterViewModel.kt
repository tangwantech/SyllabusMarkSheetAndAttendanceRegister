package com.example.syllabusmarksheetandattendanceregister.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syllabusmarksheetandattendanceregister.datamodels.StudentAttendanceData
import com.example.syllabusmarksheetandattendanceregister.datamodels.StudentsAttendanceData
import com.example.syllabusmarksheetandattendanceregister.repositories.AttendanceRegisterRepository
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AttendanceRegisterViewModel : ViewModel() {

    private val repository = AttendanceRegisterRepository()

    private val _years = MutableLiveData<List<String>>()
    val years: LiveData<List<String>> = _years

    private val _subjects = MutableLiveData<List<String>>()
    val subjects: LiveData<List<String>> = _subjects

    private val _mainClasses = MutableLiveData<List<String>>()
    val mainClasses: LiveData<List<String>> = _mainClasses

    private val _subclasses = MutableLiveData<List<String>>()
    val subclasses: LiveData<List<String>> = _subclasses

    private val _selectedYear = MutableLiveData<String?>()
    val selectedYear: LiveData<String?> = _selectedYear

    private val _selectedSubject = MutableLiveData<String?>()
    val selectedSubject: LiveData<String?> = _selectedSubject

    private val _selectedMainClass = MutableLiveData<String?>()
    val selectedMainClass: LiveData<String?> = _selectedMainClass

    private val _selectedSubclass = MutableLiveData<String?>()
    val selectedSubclass: LiveData<String?> = _selectedSubclass

    private val _selectedDate = MutableLiveData<String?>()
    val selectedDate: LiveData<String?> = _selectedDate

    private val _selectedAbsenceWeight = MutableLiveData<String?>()
    val selectedAbsenceWeight: LiveData<String?> = _selectedAbsenceWeight

    private val _students = MutableLiveData<List<StudentAttendanceData>?>()
    val students: LiveData<List<StudentAttendanceData>?> = _students

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _navigateToRegisterEvent = MutableLiveData<Boolean>(false)
    val navigateToRegisterEvent: LiveData<Boolean> = _navigateToRegisterEvent

    private val _selectedStudent = MutableLiveData<StudentAttendanceData?>()
    val selectedStudent: LiveData<StudentAttendanceData?> = _selectedStudent

    private val _navigateToDetailsEvent = MutableLiveData<Boolean>(false)
    val navigateToDetailsEvent: LiveData<Boolean> = _navigateToDetailsEvent

    init {
        observeUserData()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            UserRepository.userDataFlow.collectLatest { userData ->
                if (userData != null) {
                    loadInitialData()
                }
            }
        }
    }

    fun loadInitialData() {
        UserRepository.getAcademicYears()?.let { _years.value = it }
        UserRepository.getSubjectsTaught()?.let { _subjects.value = it }

        _selectedSubject.value?.let { subject ->
            UserRepository.getSubjectMainClasses(subject)?.let { _mainClasses.value = it.sorted() }
            _selectedMainClass.value?.let { mainClass ->
                UserRepository.getSubclasses(subject, mainClass)?.let { _subclasses.value = it.sorted() }
            }
        }
    }

    fun selectYear(year: String) { _selectedYear.value = year }

    fun selectSubject(subject: String) {
        _selectedSubject.value = subject
        _selectedMainClass.value = null
        _selectedSubclass.value = null
        val classes = UserRepository.getSubjectMainClasses(subject) ?: emptyList()
        _mainClasses.value = classes.sorted()
        _subclasses.value = emptyList()
    }

    fun selectMainClass(mainClass: String) {
        _selectedMainClass.value = mainClass
        _selectedSubclass.value = null
        val subject = _selectedSubject.value ?: return
        val subclasses = UserRepository.getSubclasses(subject, mainClass) ?: emptyList()
        _subclasses.value = subclasses.sorted()
    }

    fun selectSubclass(subclass: String) { _selectedSubclass.value = subclass }
    
    fun selectDate(date: String) { _selectedDate.value = date }

    fun selectAbsenceWeight(absenceWeight: String) { _selectedAbsenceWeight.value = absenceWeight }

    fun fetchAttendanceRegister() {
        val sessionToken = UserRepository.getSessionToken() ?: return
        val year = _selectedYear.value ?: return
        val subject = _selectedSubject.value ?: return
        val mainClass = _selectedMainClass.value ?: return
        val subclass = _selectedSubclass.value ?: return
        val date = _selectedDate.value ?: return

        _isLoading.postValue(true)
        _error.postValue(null)
        
        repository.fetchAttendanceRegister(sessionToken, year, mainClass, subclass, subject, date,
            object : AttendanceRegisterRepository.FetchAttendanceRegisterListener {

                override fun onFetchSuccessful(students: List<StudentAttendanceData>) {
                    val sortedStudents = students.sortedBy { it.name.lowercase() }
                    _students.postValue(sortedStudents)
                    _navigateToRegisterEvent.postValue(true)
                    _isLoading.postValue(false)
                }

                override fun onError(error: String) {
                    _error.postValue(error)
                    _isLoading.postValue(false)
                }
            })
    }

    fun updateStudentAttendance(position: Int, isPresent: Boolean) {
        val currentStudents = _students.value ?: return
        if (position in currentStudents.indices) {
            val student = currentStudents[position]
            val date = _selectedDate.value ?: return
            
            val newAttendances = student.attendances?.let { HashMap(it) } ?: hashMapOf()
            newAttendances[date] = com.example.syllabusmarksheetandattendanceregister.datamodels.Attendance(
                isPresent = isPresent,
                absenceCount = _selectedAbsenceWeight.value?.toIntOrNull() ?: 0
            )
            
            val updatedStudent = student.copy(attendances = newAttendances)
            val updatedList = currentStudents.toMutableList()
            updatedList[position] = updatedStudent
            val sortedList = updatedList.sortedBy { it.name.lowercase() }
            _students.value = sortedList
        }
    }

    fun saveAttendanceRegister(listener: AttendanceRegisterRepository.SaveAttendanceRegisterListener) {
        val sessionToken = UserRepository.getSessionToken() ?: return
        val year = _selectedYear.value ?: return
        val subject = _selectedSubject.value ?: return
        val mainClass = _selectedMainClass.value ?: return
        val subclass = _selectedSubclass.value ?: return
        val students = _students.value ?: return
        
        val studentsAttendanceData = StudentsAttendanceData(students)

        _isLoading.postValue(true)
        repository.saveAttendanceRegister(sessionToken, year, mainClass, subclass, subject, studentsAttendanceData, 
            object : AttendanceRegisterRepository.SaveAttendanceRegisterListener {
                override fun onSaveSuccessful(result: String) {
                    _isLoading.postValue(false)
                    listener.onSaveSuccessful(result)
                }

                override fun onError(error: String) {
                    _isLoading.postValue(false)
                    listener.onError(error)
                }
            })
    }

    fun onNavigatedToRegister() {
        _navigateToRegisterEvent.value = false
    }

    fun selectStudent(position: Int) {
        val student = _students.value?.getOrNull(position) ?: return
        _selectedStudent.value = student
        _navigateToDetailsEvent.value = true
    }

    fun onNavigatedToDetails() {
        _navigateToDetailsEvent.value = false
    }
}
