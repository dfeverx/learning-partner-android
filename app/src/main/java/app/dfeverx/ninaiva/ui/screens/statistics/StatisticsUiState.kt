package app.dfeverx.ninaiva.ui.screens.statistics

data class StatisticsUiState(
    val stage: Int,
    val score: Int,
    val accuracy: Int,
    val isSuccessfulAttempt: Boolean,
    var isFirstAttempt: Boolean? = null,
    var nextAttemptDate: Long = 0,
    var levelOverAllProgress: Float = 0f
)