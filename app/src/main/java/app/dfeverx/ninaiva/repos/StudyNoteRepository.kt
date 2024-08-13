package app.dfeverx.ninaiva.repos

import android.util.Log
import app.dfeverx.ninaiva.db.AppDatabase
import app.dfeverx.ninaiva.models.local.FlashCard
import app.dfeverx.ninaiva.models.local.StudyNote
import app.dfeverx.ninaiva.models.local.genLevelUntilCurrentStage
import app.dfeverx.ninaiva.models.remote.StudyNoteWithQuestionsFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StudyNoteRepository @Inject constructor(private val appDatabase: AppDatabase) {
    private val TAG = "StudyNoteRepository"

    fun studyNoteById(studyNoteId: String): Flow<StudyNote> {
        return appDatabase.studyNotesDao().studyNoteById(studyNoteId)
    }

    suspend fun addStudyNoteAndQuestionsFromFirestore(content: StudyNoteWithQuestionsFirestore) {
        val firebaseStorage = Firebase.storage

//        todo:convert to db transaction
        try {
            val localStudyNote = content.toStudyNoteLocal(firebaseStorage)
            val localQuestions = content.toQuestionsLocal()
            val localFlashCards = content.toFlashCardLocal()
            val snDbRes = appDatabase.studyNotesDao().addStudyNote(localStudyNote)
            val qstnDbRes = appDatabase.questionDao().addQuestions(localQuestions)
            val flashRes = appDatabase.flashDao().addFlashCards(localFlashCards)
//            todo: read all question from db localQuestions
            val questionsFromDb = appDatabase.questionDao().allQuestionsByStudyNoteId(
                localStudyNote.id
            )
//            create initial level
            val levelDbRes =
                appDatabase.levelDao()
                    .addLevels(localStudyNote.genLevelUntilCurrentStage(questionsFromDb))
            Log.d(
                TAG,
                "addStudyNoteAndQuestionsFromFirestore: inserted sn $snDbRes, qs $qstnDbRes , level $levelDbRes,flash res $flashRes"
            )
        } catch (e: Exception) {
            Log.d(TAG, "insertStudyNotesContent: $e")
        }
    }

    suspend fun addStudyNote(studyNote: StudyNote): Long {
        return appDatabase.studyNotesDao().addStudyNote(studyNote)
    }

    fun flashCardsByStudyNoteId(studyNoteId: String): Flow<List<FlashCard>> {
        return appDatabase.flashDao().flashCardsByStudyNoteId(studyNoteId)
    }

    fun updateStudyNoteLocalDoc(studyNoteId: String, absolutePath: String): Int {
        return appDatabase.studyNotesDao().updateLocalDoc(studyNoteId, absolutePath)
    }

    suspend fun retrying(id: String): Int {
        return appDatabase.studyNotesDao().retrying(id, true)
    }

    suspend fun notesWithProssesing(): List<StudyNote> {
        return appDatabase.studyNotesDao().notesWithProcessing()
    }

    suspend fun updatePinned(id: String, pinnedState: Boolean): Int {
        return appDatabase.studyNotesDao().updatePinned(id, pinnedState)
    }

    suspend fun updateStarred(id: String, starredState: Boolean): Int {
        return appDatabase.studyNotesDao().updateStarred(id, starredState)
    }

    suspend fun allStudyNoted(): List<StudyNote> {
        return appDatabase.studyNotesDao().getAllStudyNotes()
    }

    suspend fun resetNoteProgress(noteId: String): Int {
//   todo:     update in firestore
//   todo:     delete level except 1
        appDatabase.levelDao().deleteAllLevelExceptFirstOne(noteId = noteId)
        return appDatabase.studyNotesDao().resetNoteProgress(noteId)
    }

    suspend fun markedAsFailed(studyNoteId: String): Int {
        return appDatabase.studyNotesDao().updateFailed(studyNoteId)
    }

    suspend fun makeLoadingToRetry(ids: List<String>) {
        ids.map {
            appDatabase.studyNotesDao().updateFailed(it)
        }

    }

    fun allStudyNoteCount(): Flow<Int> {
        return appDatabase.studyNotesDao().allStudyNoteCount()
    }


}

