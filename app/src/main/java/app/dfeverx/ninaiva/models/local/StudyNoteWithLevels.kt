package app.dfeverx.ninaiva.models.local

import androidx.room.Embedded
import androidx.room.Relation
import kotlin.math.ceil


class StudyNoteWithLevels {
    @Embedded
    var studyNote: StudyNote? = null

    @Relation(parentColumn = "id", entityColumn = "studyNoteId", entity = Level::class)
    var levels: List<Level>? = null


}

fun StudyNote.genNextLevel(allQuestions: List<Question>, stage: Int = 1): Level {

    val correctlyAnsweredQuestions = allQuestions.filter { it.score > 0 }.toMutableList()

    val currentLevelQuestions = mutableListOf<Question>()

    // Select questions that need to be answered correctly in this level
    val newQuestions = allQuestions.filter { question ->
        question.score < stage && question.score <= 0
    }.shuffled().take(5)

    currentLevelQuestions.addAll(newQuestions)

    // Calculate the number of correctly answered questions to repeat (20% of 5)
    val repeatCount = ceil(5 * 0.2).toInt()

    if (repeatCount > 0) {
        val repeatQuestions = correctlyAnsweredQuestions.shuffled().take(repeatCount)
        currentLevelQuestions.addAll(repeatQuestions)
    }

    // Ensure level contains exactly 5 questions
    if (currentLevelQuestions.size < 5) {
        val additionalQuestions = allQuestions
            .filter { it !in currentLevelQuestions }
            .sortedBy { it.score }
            .take(5 - currentLevelQuestions.size)
        currentLevelQuestions.addAll(additionalQuestions)
    }

    currentLevelQuestions.shuffle()

    return Level(
//        id = System.currentTimeMillis(),
        stage = stage,
        questionIds = currentLevelQuestions.map { it.id },
        isCompleted = false,
        studyNoteId = this.id,
    )
}

fun StudyNote.genLevelUntilCurrentStage(allQuestions: List<Question>): List<Level> {
    val levelList = mutableListOf<Level>()


    for (i in 1..this.currentStage) {

        val correctlyAnsweredQuestions = allQuestions.filter { it.score > 0 }.toMutableList()

        val currentLevelQuestions = mutableListOf<Question>()

        // Select questions that need to be answered correctly in this level
        val newQuestions = allQuestions.filter { question ->
            question.score < i && question.score <= 0
        }.shuffled().take(5)

        currentLevelQuestions.addAll(newQuestions)

        // Calculate the number of correctly answered questions to repeat (20% of 5)
        val repeatCount = ceil(5 * 0.2).toInt()

        if (repeatCount > 0) {
            val repeatQuestions = correctlyAnsweredQuestions.shuffled().take(repeatCount)
            currentLevelQuestions.addAll(repeatQuestions)
        }

        // Ensure level contains exactly 5 questions
        if (currentLevelQuestions.size < 5) {
            val additionalQuestions = allQuestions
                .filter { it !in currentLevelQuestions }
                .sortedBy { it.score }
                .take(5 - currentLevelQuestions.size)
            currentLevelQuestions.addAll(additionalQuestions)
        }

        currentLevelQuestions.shuffle()

        levelList.add(
            Level(
//                id = System.currentTimeMillis(),
                stage = i,
                questionIds = currentLevelQuestions.map { it.id },
                isCompleted = true,
                studyNoteId = this.id,
            )
        )

    }
    return levelList

}
