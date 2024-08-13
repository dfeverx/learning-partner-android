package app.dfeverx.ninaiva.ui.screens.play

import app.dfeverx.ninaiva.models.local.Option
import app.dfeverx.ninaiva.models.local.Question

data class PlayUiState(
    val stage: Int,
    val questions: List<Question>? = null,
    val totalQuestionSize: Int = 0,
    var currentQuestionIndex: Int = -1,
    var attempt: List<Option> = listOf(),
    var score: Int = 0,
    var totalQuestionsAttempted: Int = 0
) {
    fun validateAttempt(isNegativeScoreForWrongAttemptEnabled: Boolean = false): Boolean {
        val result = this.attempt.find { atmpt -> !atmpt.isCorrect }
        if (result == null && this.attempt.isNotEmpty()) {
            this.score += 1
        } else {
//            score not less than 0
            if (this.score > 0 && isNegativeScoreForWrongAttemptEnabled) {
                this.score -= 1
            }
        }
//        add attempt, only if no options are selected
        if (this.attempt.isNotEmpty()) {
            totalQuestionsAttempted += totalQuestionsAttempted
        }
        return result == null && this.attempt.isNotEmpty()
    }

    fun currentQuestion(): Question? {
        return questions?.get(this.currentQuestionIndex)
    }

    fun progress(): Float {
        val r = this.currentQuestionIndex.toFloat() / (this.totalQuestionSize /*- 1*/)
        return if (this.totalQuestionSize == 0) 0f else r
    }


}