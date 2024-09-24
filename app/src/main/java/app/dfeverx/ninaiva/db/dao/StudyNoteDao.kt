package app.dfeverx.ninaiva.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import app.dfeverx.ninaiva.models.local.StudyNote
import app.dfeverx.ninaiva.models.local.StudyNoteWithQuestions
import kotlinx.coroutines.flow.Flow


@Dao
interface StudyNoteDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addStudyNote(studyNote: StudyNote): Long

    @Query("SELECT * FROM study_notes WHERE id = :studyNoteId")
    fun studyNoteById(studyNoteId: String): Flow<StudyNote>

    @Query("SELECT * FROM study_notes WHERE id = :studyNoteId")
    suspend fun studyNoteByIdOneShot(studyNoteId: String): StudyNote

    @Query(
        "SELECT * FROM study_notes   WHERE  (:subject IS 'all'  OR " +
                " :subject = 'starred' AND isStarred = 1 OR" +
                " :subject = 'trashed' AND isTrashed = 1 OR" +
                " :subject = 'archived' AND isInLTM = 1 OR" +
                " :subject = 'learning' AND isInLTM = 0 OR" +
                " subject = :subject) ORDER BY isProcessing DESC, isOpened DESC, isPinned DESC , createdAt DESC "
    )
    fun studyNotes(subject: String): Flow<List<StudyNote>>


    @Transaction
    @Query("SELECT * FROM study_notes WHERE id = :noteId")
    suspend fun studyNoteWithQuestions(noteId: String): StudyNoteWithQuestions?

    @Query("UPDATE study_notes SET score =:score, accuracy=:accuracy, currentStage = :stage ,nextLevelIn = :nextLevelIn,isInLTM=:isInLTM  WHERE id=:studyNoteId")
    suspend fun updateStageNextAttemptScoreAccuracy(
        studyNoteId: String,
        stage: Int,
        nextLevelIn: Long,
        score: Int,
        accuracy: Int,
        isInLTM: Boolean = false
    ): Int


    @Query("SELECT DISTINCT subject from study_notes")
    fun subjectsInStudyNotes(): Flow<List<String>>

    @Query("UPDATE study_notes SET docLocalUrl = :absolutePath  WHERE id=:studyNoteId")
    fun updateLocalDoc(studyNoteId: String, absolutePath: String): Int

    @Query("UPDATE study_notes SET docUrl = :storagePath  WHERE id=:noteId")
    suspend fun updatePdfStoragePath(noteId: String, storagePath: String): Int

    @Query("UPDATE study_notes SET isProcessing = :isProcessing  WHERE id=:id")
    suspend fun retrying(id: String, isProcessing: Boolean): Int

    @Query("SELECT * FROM study_notes   WHERE  isProcessing=:processing  ORDER BY createdAt DESC ")
    suspend fun notesWithProcessing(processing: Boolean = true): List<StudyNote>

    @Query("UPDATE study_notes SET isPinned = :pinnedState  WHERE id=:id")
    suspend fun updatePinned(id: String, pinnedState: Boolean): Int

    @Query("UPDATE study_notes SET isStarred = :starredState  WHERE id=:id")
    suspend fun updateStarred(id: String, starredState: Boolean): Int

    @Query("SELECT * FROM study_notes WHERE nextLevelIn != 0")
    suspend fun getAllStudyNotes(): List<StudyNote>

    @Query("SELECT * FROM study_notes WHERE nextLevelIn == 0 AND status == 4 AND currentStage == 1 ")
    suspend fun getAllUnattemptedStudyNotes(): List<StudyNote>




    @Query("UPDATE study_notes SET   currentStage=1 , nextLevelIn=0  WHERE id=:noteId")
    suspend fun resetNoteProgress(noteId: String): Int

    @Query("UPDATE study_notes SET   score=:score , accuracy=:accuracy  WHERE id=:studyNoteId")
    suspend fun updateLastScoreAndAccuracy(studyNoteId: String, score: Int, accuracy: Int): Int

    @Query("UPDATE study_notes SET   isProcessing=:isProcessing, status=:status   WHERE id=:studyNoteId")
    suspend fun updateFailed(
        studyNoteId: String,
        isProcessing: Boolean = false,
        status: Int = 1
    ): Int

    @Query("SELECT COUNT(*) FROM study_notes")
    fun allStudyNoteCount(): Flow<Int>


}