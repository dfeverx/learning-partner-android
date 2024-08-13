package app.dfeverx.ninaiva.models.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.dfeverx.ninaiva.db.converter.LongListConverter

@Entity(
    tableName = "levels",
    foreignKeys = [ForeignKey(
        entity = StudyNote::class,
        parentColumns = ["id"],
        childColumns = ["studyNoteId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Level(

    val stage: Int,//serially numbered from 1
    val studyNoteId: String,
    @TypeConverters(LongListConverter::class)
    val questionIds: List<Long>,
    val isCompleted: Boolean,
    val isRevision: Boolean = false
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var accuracy: Int = 0
}

