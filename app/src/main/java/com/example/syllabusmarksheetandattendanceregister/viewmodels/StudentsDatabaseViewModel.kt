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

    private val _studentsToDelete = MutableLiveData<MutableList<StudentData>>(mutableListOf())
    val studentsToDelete: LiveData<MutableList<StudentData>> = _studentsToDelete

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

        _isLoading.postValue(true)
        _error.postValue(null)

        repository.fetchStudents(sessionToken, year, subject, mainClass, subclass,
            object : StudentDatabaseRepository.FetchStudentsListener {
                override fun onStudentsAvailable(students: List<StudentData>) {
                    val sortedStudents = students.sortedBy { it.name.lowercase() }
                    _students.postValue(sortedStudents)
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
        
        val currentList = _students.value?.toMutableList() ?: mutableListOf()
        
        // Check if student already exists in the list by matricule
        val exists = currentList.any { it.matricule.equals(matricule, ignoreCase = true) }
        if (exists) {
            _error.value = "Student with matricule $matricule already exists"
            return
        }

        val newStudent = StudentData(year, name, matricule, gender, subject, mainClass, subclass)
        currentList.add(newStudent)
        val sortedList = currentList.sortedBy { it.name.lowercase() }
        _students.postValue(sortedList)
    }

    fun updateStudent(index: Int, name: String, matricule: String, gender: String) {
        val currentList = _students.value?.toMutableList() ?: return
        if (index !in currentList.indices) return

        // Check if student already exists in the list by matricule (excluding itself)
        val exists = currentList.filterIndexed { i, _ -> i != index }
            .any { it.matricule.equals(matricule, ignoreCase = true) }
        if (exists) {
            _error.value = "Student with matricule $matricule already exists"
            return
        }

        val oldStudent = currentList[index]
        currentList[index] = oldStudent.copy(name = name, matricule = matricule, gender = gender)
        val sortedList = currentList.sortedBy { it.name.lowercase() }
        _students.postValue(sortedList)
    }

    fun saveStudents(listener: StudentDatabaseRepository.AddStudentsListener) {
        val sessionToken = UserRepository.getSessionToken() ?: return
        val year = _selectedYear.value ?: return
        val subject = _selectedSubject.value ?: return
        val mainClass = _selectedMainClass.value ?: return
        val subclass = _selectedSubclass.value ?: return
        val currentList = _students.value ?: return

        _isLoading.postValue(true)
        _error.postValue(null)

        repository.addStudents(sessionToken, year, subject, mainClass, subclass, currentList,
            object : StudentDatabaseRepository.AddStudentsListener {
                override fun onStudentsAdded(result: String) {
                    _isLoading.postValue(false)
                    listener.onStudentsAdded(result)
                }

                override fun onError(error: String) {
                    _isLoading.postValue(false)
                    _error.postValue(error)
                    listener.onError(error)
                }
            })
    }

    fun clearStudentsToDelete(){
        _studentsToDelete.value?.clear()
    }

    fun onNavigatedToDatabase() {
        _navigateToDatabaseEvent.value = false
    }

    private val _navigateToDeleteEvent = MutableLiveData<Int?>(null)
    val navigateToDeleteEvent: LiveData<Int?> = _navigateToDeleteEvent

    fun onLongPressStudent(index: Int) {
        _navigateToDeleteEvent.value = index
    }

    fun onNavigatedToDelete() {
        _navigateToDeleteEvent.value = null
    }

    fun toggleStudentSelection(index: Int, isSelected: Boolean) {
        val currentDeleteList = _studentsToDelete.value ?: mutableListOf()
        val student = _students.value?.get(index) ?: return
        
        if (isSelected) {
            if (!currentDeleteList.any { it.matricule == student.matricule }) {
                currentDeleteList.add(student)
            }
        } else {
            currentDeleteList.removeAll { it.matricule == student.matricule }
        }
        _studentsToDelete.value = currentDeleteList
    }

    fun deleteSelectedStudents(listener: StudentDatabaseRepository.DeleteStudentsListener) {
        val sessionToken = UserRepository.getSessionToken() ?: return
        val toDelete = _studentsToDelete.value ?: return
        if (toDelete.isEmpty()) return

        _isLoading.postValue(true)
        repository.deleteStudents(sessionToken, toDelete, object : StudentDatabaseRepository.DeleteStudentsListener {
            override fun onDeleteSuccessful(result: String) {
                // Refresh list
                fetchStudents()
                _studentsToDelete.postValue(mutableListOf())
                listener.onDeleteSuccessful(result)
            }

            override fun onError(error: String) {
                _isLoading.postValue(false)
                listener.onError(error)
            }
        })
    }
}