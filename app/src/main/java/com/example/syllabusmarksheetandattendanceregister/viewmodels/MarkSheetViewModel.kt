package com.example.syllabusmarksheetandattendanceregister.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syllabusmarksheetandattendanceregister.datamodels.MarkSheetData
import com.example.syllabusmarksheetandattendanceregister.repositories.MarkSheetRepository
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MarkSheetViewModel : ViewModel() {

    private val markSheetRepository = MarkSheetRepository()

    private val _years = MutableLiveData<List<String>>()
    val years: LiveData<List<String>> = _years

    private val _subjects = MutableLiveData<List<String>>()
    val subjects: LiveData<List<String>> = _subjects

    private val _mainClasses = MutableLiveData<List<String>>()
    val mainClasses: LiveData<List<String>> = _mainClasses

    private val _subclasses = MutableLiveData<List<String>>()
    val subclasses: LiveData<List<String>> = _subclasses

    private val _sequences = MutableLiveData<List<String>>()
    val sequences: LiveData<List<String>> = _sequences

    private val _selectedYear = MutableLiveData<String?>()
    val selectedYear: LiveData<String?> = _selectedYear

    private val _selectedSubject = MutableLiveData<String?>()
    val selectedSubject: LiveData<String?> = _selectedSubject

    private val _selectedMainClass = MutableLiveData<String?>()
    val selectedMainClass: LiveData<String?> = _selectedMainClass

    private val _selectedSubclass = MutableLiveData<String?>()
    val selectedSubclass: LiveData<String?> = _selectedSubclass

    private val _selectedSequence = MutableLiveData<String?>()
    val selectedSequence: LiveData<String?> = _selectedSequence

    private val _markSheetData = MutableLiveData<MarkSheetData?>()
    val markSheetData: LiveData<MarkSheetData?> = _markSheetData

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _navigateToMarkSheetEvent = MutableLiveData<Boolean>(false)
    val navigateToMarkSheetEvent: LiveData<Boolean> = _navigateToMarkSheetEvent

    private var isDataSaved = false

    init {
        observeUserData()
        _sequences.value = listOf("Sequence 1", "Sequence 2", "Sequence 3", "Sequence 4", "Sequence 5", "Sequence 6")
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
    fun selectSequence(sequence: String) { _selectedSequence.value = sequence }

    fun fetchMarkSheet() {
        val sessionToken = UserRepository.getSessionToken() ?: return
        val year = _selectedYear.value ?: return
        val subject = _selectedSubject.value ?: return
        val mainClass = _selectedMainClass.value ?: return
        val subclass = _selectedSubclass.value ?: return
        val sequence = _selectedSequence.value ?: return

        _isLoading.postValue(true)
        markSheetRepository.fetchMarkSheet(sessionToken, year, subject, mainClass, subclass, sequence, 
            object : MarkSheetRepository.FetchMarksheetListener {
                override fun onMarkSheetAvailable(result: MarkSheetData) {
                    val sortedResult = result.copy(students = result.students.sortedBy { it.name.lowercase() })
                    _markSheetData.postValue(sortedResult)
                    _navigateToMarkSheetEvent.postValue(true)
                    _isLoading.postValue(false)
                }

                override fun onError(error: String) {
                    _error.postValue(error)
                    _isLoading.postValue(false)
                }
            })
    }

    fun updateStudentScore(studentIndex: Int, score: Double) {
        val currentData = _markSheetData.value ?: return
        val students = currentData.students.toMutableList()
        if (studentIndex in students.indices) {
            students[studentIndex] = students[studentIndex].copy(score = score)
            val sortedStudents = students.sortedBy { it.name.lowercase() }
            _markSheetData.value = currentData.copy(students = sortedStudents)
        }
    }

    fun updateStudentRegistration(studentIndex: Int, isRegistered: Boolean) {
        val currentData = _markSheetData.value ?: return
        val students = currentData.students.toMutableList()
        students[studentIndex].isRegistered = isRegistered
        val sortedStudents = students.sortedBy { it.name.lowercase() }
        _markSheetData.value = currentData.copy(students = sortedStudents)
    }

    fun saveMarkSheet(listener: MarkSheetRepository.UpdateMarkSheetListener) {
        val sessionToken = UserRepository.getSessionToken() ?: return
        val year = _selectedYear.value ?: return
        val subject = _selectedSubject.value ?: return
        val mainClass = _selectedMainClass.value ?: return
        val subclass = _selectedSubclass.value ?: return
        val sequence = _selectedSequence.value ?: return
        val students = _markSheetData.value?.students ?: return

        _isLoading.postValue(true)
        markSheetRepository.updateMarkSheet(sessionToken, year, subject, mainClass, subclass, sequence, students, 
            object : MarkSheetRepository.UpdateMarkSheetListener {
                override fun onUpdateSuccessful(result: String) {
                    _isLoading.postValue(false)
                    listener.onUpdateSuccessful(result)
                }

                override fun onError(error: String) {
                    _isLoading.postValue(false)
                    listener.onError(error)
                }
            })
    }

    fun onNavigatedToMarkSheet() {
        _navigateToMarkSheetEvent.value = false
    }

    fun isFormValid(): Boolean {
        return _selectedYear.value != null &&
                _selectedSubject.value != null &&
                _selectedMainClass.value != null &&
                _selectedSubclass.value != null &&
                _selectedSequence.value != null
    }

    fun updateIsDataSave(state: Boolean){
        isDataSaved = state
    }

    fun getIsDataSaved(): Boolean{
        return isDataSaved
    }
}