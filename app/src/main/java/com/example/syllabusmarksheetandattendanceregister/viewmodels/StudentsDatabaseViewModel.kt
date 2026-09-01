package com.example.syllabusmarksheetandattendanceregister.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syllabusmarksheetandattendanceregister.datamodels.StudentData
import com.example.syllabusmarksheetandattendanceregister.repositories.StudentDatabaseRepository
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StudentsDatabaseViewModel : ViewModel() {

    private val repository = StudentDatabaseRepository()

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

    private val _students = MutableLiveData<List<StudentData>>()
    val students: LiveData<List<StudentData>> = _students

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _navigateToDatabaseEvent = MutableLiveData<Boolean>(false)
    val navigateToDatabaseEvent: LiveData<Boolean> = _navigateToDatabaseEvent

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

    fun fetchStudents() {
        val sessionToken = UserRepository.getSessionToken() ?: return
        val year = _selectedYear.value ?: return
        val subject = _selectedSubject.value ?: return
        val mainClass = _selectedMainClass.value ?: return
        val subclass = _selectedSubclass.value ?: return

        _isLoading.value = true
        _error.value = null

        repository.fetchStudents(sessionToken, year, subject, mainClass, subclass,
            object : StudentDatabaseRepository.FetchStudentsListener {
                override fun onStudentsAvailable(students: List<StudentData>) {
                    _students.postValue(students)
                    _navigateToDatabaseEvent.postValue(true)
                    _isLoading.postValue(false)
                }

                override fun onError(error: String) {
                    _error.postValue(error)
                    _isLoading.postValue(false)
                }
            })
    }

    fun addStudent(name: String, matricule: String, gender: String) {
        val year = _selectedYear.value ?: return
        val subject = _selectedSubject.value ?: return
        val mainClass = _selectedMainClass.value ?: return
        val subclass = _selectedSubclass.value ?: return
        
        val newStudent = StudentData(year, name, matricule, gender, subject, mainClass, subclass)
        val currentList = _students.value?.toMutableList() ?: mutableListOf()
        currentList.add(newStudent)
        
        _isLoading.value = true
        val sessionToken = UserRepository.getSessionToken() ?: return
        
        repository.addStudents(sessionToken, year, subject, mainClass, subclass, currentList, 
            object : StudentDatabaseRepository.AddStudentsListener {
                override fun onStudentsAdded(result: String) {
                    _students.postValue(currentList)
                    _isLoading.postValue(false)
                }

                override fun onError(error: String) {
                    _error.postValue(error)
                    _isLoading.postValue(false)
                }
            })
    }

    fun onNavigatedToDatabase() {
        _navigateToDatabaseEvent.value = false
    }
}