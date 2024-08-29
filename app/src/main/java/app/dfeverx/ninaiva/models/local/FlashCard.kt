package app.dfeverx.ninaiva.models.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity(
    tableName = "flash_cards",
    foreignKeys = [ForeignKey(
        entity = StudyNote::class,
        parentColumns = ["id"],
        childColumns = ["studyNoteId"],
        onDelete = ForeignKey.CASCADE
    )]
)
class FlashCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val emoji: String = "",
    val prompt: String = "",
    val info: String = "",
    val studyNoteId: String = ""
) {
    var flagged = false
    var lastAttemptedIn: Long = 0
    var areaId = -1
    var repetitions: Int = 0
    @Ignore
    var isPlaceholder: Boolean = false
    //    if the attempt is correct it increase by one other wise decrease by one
    var score: Int = 0
}