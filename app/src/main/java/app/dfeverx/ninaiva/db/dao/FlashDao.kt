package app.dfeverx.ninaiva.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.dfeverx.ninaiva.models.local.FlashCard
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFlashCards(questions: List<FlashCard>): List<Long>

    @Query("SELECT * FROM flash_cards WHERE studyNoteId =:studyNoteId")
    fun flashCardsByStudyNoteId(studyNoteId: String): Flow<List<FlashCard>>



}