package com.example.syllabusmarksheetandattendanceregister.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.syllabusmarksheetandattendanceregister.datamodels.ChapterData
import com.example.syllabusmarksheetandattendanceregister.datamodels.ChaptersData
import com.example.syllabusmarksheetandattendanceregister.datamodels.Lesson
import com.example.syllabusmarksheetandattendanceregister.repositories.SyllabusChaptersRepository
import com.example.syllabusmarksheetandattendanceregister.repositories.UserRepository
import com.example.syllabusmarksheetandattendanceregister.database.AppDatabase
import com.example.syllabusmarksheetandattendanceregister.database.SyllabusEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SyllabusViewModel : ViewModel() {

    private val syllabusChaptersRepository = SyllabusChaptersRepository()

    private val _years = MutableLiveData<List<String>>()
    val years: LiveData<List<String>> = _years

    private val _subjects = MutableLiveData<List<String>>()
    val subjects: LiveData<List<String>> = _subjects

    private val _mainClasses = MutableLiveData<List<String>>()
    val mainClasses: LiveData<List<String>> = _mainClasses

    private val _selectedYear = MutableLiveData<String?>()
    val selectedYear: LiveData<String?> = _selectedYear

    private val _selectedSubject = MutableLiveData<String?>()
    val selectedSubject: LiveData<String?> = _selectedSubject

    private val _selectedMainClass = MutableLiveData<String?>()
    val selectedMainClass: LiveData<String?> = _selectedMainClass

    private val _chapters = MutableLiveData<List<ChapterData>?>()
    val chapters: LiveData<List<ChapterData>?> = _chapters

    private val _chapterLessons = MutableLiveData<List<Lesson>>()
    val chapterLessons: LiveData<List<Lesson>> = _chapterLessons

    private val _selectedChapter = MutableLiveData<ChapterData?>()
    val selectedChapter: LiveData<ChapterData?> = _selectedChapter

    private var _selectedChapterIndex = MutableLiveData<Int>(-1)
    val selectedChapterIndex: LiveData<Int> = _selectedChapterIndex

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _navigateToChaptersEvent = MutableLiveData<Boolean>(false)
    val navigateToChaptersEvent: LiveData<Boolean> = _navigateToChaptersEvent

    data class SyllabusProgress(
        val totalLessons: Int,
        val completedLessons: Int,
        val percentage: Double
    )

    private val _overallProgress = MutableLiveData<SyllabusProgress>()
    val overallProgress: LiveData<SyllabusProgress> = _overallProgress

//    private var _lessonsDoneCount = MutableLiveData<Int>(0)
//    val lessonsDoneCount: LiveData<Int> = _lessonsDoneCount

    private var _selectChapterLessonsCount = MutableLiveData<Int>(0)
    val selectChapterLessonsCount: LiveData<Int> = _selectChapterLessonsCount

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
        }
    }

    fun selectYear(year: String) {
        _selectedYear.value = year
    }

    fun selectSubject(subject: String) {
        _selectedSubject.value = subject
        _selectedMainClass.value = null
        val classes = UserRepository.getSubjectMainClasses(subject) ?: emptyList()
        _mainClasses.value = classes.sorted()
    }

    fun selectMainClass(mainClass: String) {
        _selectedMainClass.value = mainClass
    }

//    fun selectChapter(chapter: ChapterData) {
//        _selectedChapter.value = chapter
//        _selectedChapterIndex.value = _chapters.value?.indexOfFirst { it.chapter == chapter.chapter } ?: -1
//    }

    fun fetchSyllabusChapters() {
        val year = _selectedYear.value ?: return
        val subject = _selectedSubject.value ?: return
        val mainClass = _selectedMainClass.value ?: return
        val sessionToken = UserRepository.getSessionToken() ?: return

        _isLoading.postValue(true)
        syllabusChaptersRepository.getSyllabusCoverage(
            sessionToken, year, subject, mainClass,
            object : SyllabusChaptersRepository.GetSyllabusListener {
                override fun onSyllabusAvailable(chapters: List<ChapterData>) {
                    _chapters.postValue(chapters)
                    updateOverallProgress(chapters)
                    _navigateToChaptersEvent.postValue(true)
                    _isLoading.postValue(false)
                }

                override fun onError(error: String?) {
                    _error.postValue(error)
                    _isLoading.postValue(false)
                }
            }
        )
    }



//    fun fetchChapterLessons(){
//        val tempChapterLessons = _chapters.value!![_selectedChapterIndex.value!!].lessons
//        _chapterLessons.postValue(tempChapterLessons)
//
//    }

    fun updateLessonStatus(lessonIndex: Int, isTaught: Boolean) {
        val chapter = _selectedChapter.value ?: return

        _isLoading.postValue(true)

        chapter.lessons[lessonIndex].isTaught = isTaught
        updateSelectedSubjectLessonsCount()
        updateChapter()
    }

    private fun updateChapter(){
        val chapters = _chapters.value ?: return
        val index = _selectedChapterIndex.value ?: return
        val chapter = _selectedChapter.value ?: return
        
        val chaps = ArrayList<ChapterData>(chapters)
        if (index >= 0 && index < chaps.size) {
            chaps[index] = chapter
            _chapters.postValue(chaps)
            updateOverallProgress(chaps)
        }
    }

    private fun updateOverallProgress(chaptersList: List<ChapterData>) {
        var total = 0
        var completed = 0
        chaptersList.forEach { chapter ->
            total += chapter.lessons.size
            completed += chapter.lessons.count { it.isTaught }
        }
        val percentage = if (total > 0) (completed.toDouble() * 100.0) / total.toDouble() else 0.0
        _overallProgress.postValue(SyllabusProgress(total, completed, percentage))
    }

    fun updateChaptersInSyllabusChaptersRepository(context: android.content.Context, listener: SyllabusChaptersRepository.UpdateSyllabusListener){
        val sessionToken = UserRepository.getSessionToken() ?: return
        val year = _selectedYear.value ?: return
        val subject = _selectedSubject.value ?: return
        val mainClass = _selectedMainClass.value ?: return
        val chapters = _chapters.value ?: return
        val chaptersData = ChaptersData(chapters)
        
        viewModelScope.launch {
            val entity = SyllabusEntity(
                academicYear = year,
                subject = subject,
                mainClass = mainClass,
                chapters = chapters
            )
            AppDatabase.getDatabase(context.applicationContext).syllabusDao().insertSyllabus(entity)
        }
        
        syllabusChaptersRepository.updateSyllabusCoverage(sessionToken, year, subject, mainClass, chaptersData, listener)
    }

    fun clearSyllabusFromDatabase(context: android.content.Context) {
        viewModelScope.launch {
            AppDatabase.getDatabase(context.applicationContext).syllabusDao().clearSyllabus()
        }
    }

    fun isFormValid(): Boolean {
        return _selectedYear.value != null &&
               _selectedSubject.value != null &&
               _selectedMainClass.value != null
    }

    fun updateSelectedChapterIndexAndChapter(chapterIndex: Int, chapter: ChapterData) {
        _selectedChapterIndex.postValue(chapterIndex)
        _selectedChapter.postValue(chapter)

//
    }

    fun updateIsLoading(state: Boolean){
        _isLoading.postValue(state)
    }

    fun updateSelectedSubjectLessonsCount(){
        _selectedChapter.value?.let { chapter ->
            _selectChapterLessonsCount.value = chapter.lessons.count { it.isTaught }
        }
    }

    fun onNavigatedToChapters() {
        _navigateToChaptersEvent.value = false
    }

    fun clearChapters() {
        _chapters.value = null
    }


}
