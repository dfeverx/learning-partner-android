package app.dfeverx.ninaiva.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import app.dfeverx.ninaiva.models.local.Level
import app.dfeverx.ninaiva.models.local.StudyNoteWithLevels
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLevel(level: Level): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLevels(levels: List<Level>): List<Long>


    @Query("SELECT * FROM levels WHERE   studyNoteId =:noteId ")
    suspend fun levelByNoteId(noteId: String): List<Level>

    @Transaction
    @Query("SELECT * FROM study_notes WHERE id = :studyNoteId")
    fun studyNoteWithLevels(studyNoteId: String): Flow<StudyNoteWithLevels?>

    @Query("SELECT * FROM levels WHERE id = :levelId")
    suspend fun getLevelById(levelId: Long): Level

    @Query("UPDATE levels SET accuracy = :accuracy  WHERE studyNoteId=:studyNoteId AND stage=:stage")
    fun updateAccuracyAndScore(studyNoteId: String, stage: Int, accuracy: Int)

    @Query("DELETE FROM levels WHERE studyNoteId=:noteId AND stage !=1")
    suspend fun deleteAllLevelExceptFirstOne(noteId: String): Int


}