package app.dfeverx.ninaiva.repos

import android.net.Uri
import app.dfeverx.ninaiva.db.AppDatabase
import app.dfeverx.ninaiva.models.local.StudyNote
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HomeRepository @Inject constructor(private val appDatabase: AppDatabase) {
    private val TAG = "HomeRepository"
    suspend fun addNote(studyNoteId: String, pdfUri: Uri): Long {
        val si = StudyNote(id = studyNoteId).apply {
            docLocalUrl = pdfUri.toString()
        }
        return appDatabase.studyNotesDao().addStudyNote(si)
    }

    fun updateStatusOfStudyNote(studyNoteId: String, status: Int) {
//todo
    }


    fun studyNotes(subject: String): Flow<List<StudyNote>> =
        appDatabase.studyNotesDao().studyNotes(subject)


    val subjects = appDatabase.studyNotesDao().subjectsInStudyNotes()


}