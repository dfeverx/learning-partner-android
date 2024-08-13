package app.dfeverx.ninaiva.models.local

import androidx.room.Embedded
import androidx.room.Relation


class StudyNoteWithQuestions {
    @Embedded
    var studyNote: StudyNote? = null

    @Relation(parentColumn = "id", entityColumn = "studyNoteId", entity = Question::class)
    var questions: List<Question>? = null

}


