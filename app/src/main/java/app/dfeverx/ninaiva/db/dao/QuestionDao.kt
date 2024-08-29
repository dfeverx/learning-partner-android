package app.dfeverx.ninaiva.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.dfeverx.ninaiva.models.local.Question

@Dao
interface QuestionDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addQuestions(questions: List<Question>): List<Long>

    @Query("SELECT * FROM questions WHERE id IN (:questionIds)")
    suspend fun questionsByIds(questionIds: List<Long>): List<Question>

    @Query("UPDATE questions SET repetitions = repetitions + 1, score = :score WHERE id=:questionId")
    suspend fun updateScoreAndRepetitionInQuestion(questionId: Long, score: Int): Int


    @Query("SELECT * FROM questions WHERE studyNoteId =:studyNoteId")
    suspend fun allQuestionsByStudyNoteId(studyNoteId: String): List<Question>

    @Query("SELECT COUNT(id) FROM questions WHERE studyNoteId=:studyNoteId")
    suspend fun questionCount(studyNoteId: String): Int

}