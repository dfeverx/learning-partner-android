package app.dfeverx.ninaiva.models.local

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.dfeverx.ninaiva.db.converter.KeyAreasConverter
import app.dfeverx.ninaiva.db.converter.MarkdownStringListConverter
import java.time.Instant
import java.util.Calendar

@Entity(tableName = "study_notes")
class StudyNote(
    @PrimaryKey(autoGenerate = false)
    val id: String,
    var docUrl: String = "",
    var title: String = "",
    var summary: String = "",
    @TypeConverters(KeyAreasConverter::class)
    var keyAreas: List<KeyArea> = listOf(),
    var subject: String = "",
    var thumbnail: String = "",
    var markdown: String = "",
    @TypeConverters(MarkdownStringListConverter::class)//Todo: add string instead of markdownArray
    var srcLng: List<String> = listOf(),
    var totalLevel: Int = 5,
    var status: Int = 0
    /*
    * -100 : local pdf file corrupted
    * -101 : uploading pdf..
    * -102 : upload pdf to firebase storage failed
    * -103 : fun calling..
    * -104 : fun call failed
    *  */
) {
    @Ignore
    var isPlaceholder: Boolean = false
    var isProcessing: Boolean = true
    var isSuspended: Boolean = false

    var docLocalUrl = ""

    //    analytics

    var currentStage: Int = 1

    //    next repetition date in unix timestamp seconds
    var nextLevelIn: Long = 0

    //    over all score
    var score: Int = 0
    var accuracy: Int = 0

    // if all the levels are completed and all the questions are answered correctly with out any mistakes at least three times
    var isInLTM = false
    var isTrashed: Boolean = false
    var isPinned: Boolean = false
    var isStarred: Boolean = false
    var isOpened: Boolean = false

    //    todo

    var createdAt: Long = System.currentTimeMillis()
    var updatedAt: Long = System.currentTimeMillis()


}

const val NOTE_PROCESSING_LOCAL_FILE_CORRUPTED= -100
const val NOTE_PROCESSING_UPLOADING_PDF= -101
const val NOTE_PROCESSING_UPLOADING_PDF_FAILED= -102
const val NOTE_PROCESSING_FUN_CALLING= -103
const val NOTE_PROCESSING_FUN_CALLING_FAILED= -104