package com.example.syllabusmarksheetandattendanceregister.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syllabusmarksheetandattendanceregister.datamodels.StudentPerformanceStatsData
import com.example.syllabusmarksheetandattendanceregister.repositories.StudentsPerformanceStatsRepo
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StudentsPerformanceViewModel : ViewModel() {

    private val repository = StudentsPerformanceStatsRepo()

    private val _years = MutableLiveData<List<String>>()
    val years: LiveData<List<String>> = _years

    private val _subjects = MutableLiveData<List<String>>()
    val subjects: LiveData<List<String>> = _subjects

    private val _mainClasses = MutableLiveData<List<String>>()
    val mainClasses: LiveData<List<String>> = _mainClasses

    private val _sequences = MutableLiveData<List<String>>()
    val sequences: LiveData<List<String>> = _sequences

    private val _selectedYear = MutableLiveData<String?>()
    val selectedYear: LiveData<String?> = _selectedYear

    private val _selectedSubject = MutableLiveData<String?>()
    val selectedSubject: LiveData<String?> = _selectedSubject

    private val _selectedMainClass = MutableLiveData<String?>()
    val selectedMainClass: LiveData<String?> = _selectedMainClass

    private val _selectedSequence = MutableLiveData<String?>()
    val selectedSequence: LiveData<String?> = _selectedSequence

    private val _performanceData = MutableLiveData<StudentPerformanceStatsData?>()
    val performanceData: LiveData<StudentPerformanceStatsData?> = _performanceData

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _showPerformanceDialogEvent = MutableLiveData<Boolean>(false)
    val showPerformanceDialogEvent: LiveData<Boolean> = _showPerformanceDialogEvent

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
        }
    }

    fun selectYear(year: String) { _selectedYear.value = year }

    fun selectSubject(subject: String) {
        _selectedSubject.value = subject
        _selectedMainClass.value = null
        val classes = UserRepository.getSubjectMainClasses(subject) ?: emptyList()
        _mainClasses.value = classes.sorted()
    }

    fun selectMainClass(mainClass: String) {
        _selectedMainClass.value = mainClass
    }

    fun selectSequence(sequence: String) { _selectedSequence.value = sequence }

    fun fetchPerformanceStats() {
        val sessionToken = UserRepository.getSessionToken() ?: return
        val year = _selectedYear.value ?: return
        val subject = _selectedSubject.value ?: return
        val mainClass = _selectedMainClass.value ?: return
        val sequence = _selectedSequence.value ?: return

        _isLoading.postValue(true)
        repository.fetchStudentsPerformanceStats(sessionToken, year, subject, mainClass, sequence,
            object : StudentsPerformanceStatsRepo.StudentsPerformanceStatsListener {
                override fun onStudentsPerformanceStatsFetched(result: StudentPerformanceStatsData) {
                    _performanceData.postValue(result)
                    _showPerformanceDialogEvent.postValue(true)
                    _isLoading.postValue(false)
                }

                override fun onError(error: String) {
                    _error.postValue(error)
                    _isLoading.postValue(false)
                }
            })
    }

    fun onPerformanceDialogDismissed() {
        _showPerformanceDialogEvent.value = false
    }

    fun isFormValid(): Boolean {
        return _selectedYear.value != null &&
                _selectedSubject.value != null &&
                _selectedMainClass.value != null &&
                _selectedSequence.value != null
    }
}