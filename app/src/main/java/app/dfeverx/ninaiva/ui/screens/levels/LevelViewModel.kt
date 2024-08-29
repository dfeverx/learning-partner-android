package app.dfeverx.ninaiva.ui.screens.levels

import android.app.AlarmManager
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dfeverx.ninaiva.models.LEVEL_EMOJI_NAME
import app.dfeverx.ninaiva.models.local.StudyNoteWithLevels
import app.dfeverx.ninaiva.repos.LevelRepository
import app.dfeverx.ninaiva.utils.TimePeriod
import app.dfeverx.ninaiva.utils.getTimePeriod
import app.dfeverx.ninaiva.utils.hasPermission
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.ocpsoft.prettytime.PrettyTime
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LevelViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val levelRepository: LevelRepository,
    private val prettyTime: PrettyTime,
    private val alarmManager: AlarmManager
) : ViewModel() {
    private val TAG = "LevelViewModel"
    private val studyNoteId: String = checkNotNull(savedStateHandle["noteId"])

    //    todo: shimmer anim
    private val _studyNoteWithLevels: MutableStateFlow<List<LevelUI?>> = MutableStateFlow(
        listOf(
            LevelUI(
                levelId = -1,
                stage = 0,
                icon = "",
                name = "",
                isPlayable = false,
                description = "",
                nextPlayUnix = 0,
                isRevision = false,
                isPlaceholder = true
            ),
            LevelUI(
                levelId = -1,
                stage = 0,
                icon = "",
                name = "",
                isPlayable = false,
                description = "",
                nextPlayUnix = 0,
                isRevision = false,
                isPlaceholder = true
            ),
            LevelUI(
                levelId = -1,
                stage = 0,
                icon = "",
                name = "",
                isPlayable = false,
                description = "",
                nextPlayUnix = 0,
                isRevision = false,
                isPlaceholder = true
            ),
            LevelUI(
                levelId = -1,
                stage = 0,
                icon = "",
                name = "",
                isPlayable = false,
                description = "",
                nextPlayUnix = 0,
                isRevision = false,
                isPlaceholder = true
            ),
            LevelUI(
                levelId = -1,
                stage = 0,
                icon = "",
                name = "",
                isPlayable = false,
                description = "",
                nextPlayUnix = 0,
                isRevision = false,
                isPlaceholder = true
            ),
            LevelUI(
                levelId = -1,
                stage = 0,
                icon = "",
                name = "",
                isPlayable = false,
                description = "",
                nextPlayUnix = 0,
                isRevision = false,
                isPlaceholder = true
            ),
            LevelUI(
                levelId = -1,
                stage = 0,
                icon = "",
                name = "",
                isPlayable = false,
                description = "",
                nextPlayUnix = 0,
                isRevision = false,
                isPlaceholder = true
            ),
        )
    )
    val levels: MutableStateFlow<List<LevelUI?>>
        get() = _studyNoteWithLevels


    init {
        viewModelScope.launch(Dispatchers.IO) {
            delay(800)
            levelRepository.studyNoteWithQuestions(studyNoteId).collect { studyNote ->
                _studyNoteWithLevels.value = studyNote?.uiLevels(prettyTime) ?: listOf()
                Log.d(TAG, "init: $studyNoteId")
                Log.d(TAG, "init: ${studyNote?.levels}")
                Log.d(TAG, "init: ${studyNote?.studyNote}")
            }

        }
    }

    fun hasAlarmPermission(): Boolean {
        Log.d(TAG, "hasAlarmPermission: ${alarmManager.hasPermission()}")
        return alarmManager.hasPermission()
    }


}


private fun StudyNoteWithLevels.uiLevels(prettyTime: PrettyTime): List<LevelUI> {

    val listOfUiLevel = mutableListOf<LevelUI>()
    when (this.studyNote) {
        null -> {
            //todo:shimmer anim
        }

        else -> {
            val levelsWithOutRevision = this.levels?.filter { !it.isRevision }
            val levelsWithRevision = this.levels?.filter { it.isRevision }
            for (i in 1..(this.studyNote?.totalLevel!!)) {
//todo: limit i max level icon size
                listOfUiLevel.add(
                    LevelUI(
                        levelId = levelsWithOutRevision?.getOrNull(i - 1)?.id,// todo: improve
                        isRevision = levelsWithOutRevision?.getOrNull(i - 1)?.isRevision ?: false,
                        stage = i,
                        nextPlay = getTimePeriod(this.studyNote!!.nextLevelIn),
                        currentStage = this.studyNote?.currentStage ?: -1,
                        icon = LEVEL_EMOJI_NAME[i - 1].first,
                        name = LEVEL_EMOJI_NAME[i - 1].second,
                        isPlayable = this.studyNote!!.currentStage >= i,
                        description = if (i == this.studyNote?.currentStage) prettyTime.format(
                            this.studyNote?.nextLevelIn?.let {
                                Date(
                                    it
                                )
                            }
                        ) else "", nextPlayUnix = this.studyNote?.nextLevelIn ?: 0
                    )
                )
            }
            levelsWithRevision?.forEachIndexed { index, level ->
                listOfUiLevel.add(
                    LevelUI(
                        levelId = level.id,
                        stage = (this.studyNote?.totalLevel!! + index + 1),
                        icon = "",
                        name = "",
                        currentStage = this.studyNote?.currentStage ?: -1,
                        isRevision = true,
                        description = "",
                        nextPlayUnix = 0

                    )
                )
            }
        }
    }
//    revision
    /*    listOfUiLevel.add(
            LevelUI(
                levelId = null,
                stage = 0,
                icon = "",
                name = "",
                isRevision = true,
                description = "",
                nextPlayUnix = 0

            )
        )*/

    return listOfUiLevel
}

data class LevelUI(
    val levelId: Long?,
    val stage: Int,
    val icon: String,
    val name: String,
    val isPlayable: Boolean = false,
    val currentStage: Int = -1,
    val nextPlay: TimePeriod? = null,
    val nextPlayUnix: Long,
    val description: String,
    val isRevision: Boolean = false,
    val isPlaceholder: Boolean = false
)