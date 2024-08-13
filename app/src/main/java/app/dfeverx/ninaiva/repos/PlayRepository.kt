package app.dfeverx.ninaiva.repos

import android.util.Log
import app.dfeverx.ninaiva.db.AppDatabase
import app.dfeverx.ninaiva.models.local.FlashCard
import app.dfeverx.ninaiva.models.local.Question
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PlayRepository @Inject constructor(private val appDatabase: AppDatabase) {
    private val TAG = "PlayRepository"
    suspend fun questionsByLevelId(levelId: Long): List<Question> {
//        read level by level id
        val level = appDatabase.levelDao().getLevelById(levelId)
        Log.d(TAG, "questionsByLevel: level $level")
//        read list of question by ids
        val questionIds = level.questionIds
        Log.d(TAG, "questionsByLevel: $questionIds")
        val questions = appDatabase.questionDao().questionsByIds(questionIds)
        Log.d(TAG, "questionsByLevel: $questions")
        return questions
    }


    suspend fun updateAttemptInQuestion(questionId: Long, updatedScore: Int): Int {
        return appDatabase.questionDao()
            .updateScoreAndRepetitionInQuestion(questionId, updatedScore)
    }

    //    flash
    fun flashCardsByNoteId(noteId: String): Flow<List<FlashCard>> {
        return appDatabase.flashDao().flashCardsByStudyNoteId(noteId)
    }
}