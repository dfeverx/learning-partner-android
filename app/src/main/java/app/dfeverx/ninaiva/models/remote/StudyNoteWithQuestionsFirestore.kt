package app.dfeverx.ninaiva.models.remote

import android.util.Log
import app.dfeverx.ninaiva.models.local.FlashCard
import app.dfeverx.ninaiva.models.local.KeyArea
import app.dfeverx.ninaiva.models.local.Option
import app.dfeverx.ninaiva.models.local.Question
import app.dfeverx.ninaiva.models.local.StudyNote
import com.google.firebase.storage.FirebaseStorage
import com.google.protobuf.BoolValueOrBuilder
import kotlinx.coroutines.tasks.await


data class StudyNoteWithQuestionsFirestore(
    var id: String = "",
    var docUrl: String = "",
    var title: String = "",
    var summary: String = "",
    var markdown: String = "",
    var isStudyNote: Boolean = true,
    var questions: List<QuestionFirestore> = listOf(),
    var flashCards: List<FlashCardFirestore> = listOf(),
    var keyAreas: List<KeyAreaFirestore> = listOf(),
    var thumbnail: String = "",
    var subject: String = "",
    var srcLng: List<String> = listOf(),
    var status: Int = 0,
    var isProcessing: Boolean = false,
    var createdAt: Long = 0,
    var isPinned: Boolean = false,
    var isStarred: Boolean = false,
    var isOpened: Boolean = true,
    var currentStage: Int = 1,
    var nextLevelIn: Long = 0,
    var score: Int = 0,
    var accuracy: Int = 0

) {
    suspend fun toStudyNoteLocal(firebaseStorage: FirebaseStorage): StudyNote {

        val thumbUrl = if (thumbnail != "") {
            val storageRef = firebaseStorage.getReference(this.thumbnail)
            storageRef.downloadUrl.await()
        } else {
            ""
        }
        Log.d("TAG", "toStudyNoteLocal: thumb $thumbUrl")
        return StudyNote(
            id = this.id,
            docUrl = this.docUrl,
            title = this.title,
            summary = this.summary,
            markdown = this.markdown,
            keyAreas = this.keyAreas.toLocal(),
            thumbnail = thumbUrl.toString(),
            srcLng = this.srcLng,
            subject = this.subject,
            totalLevel = (this.questions.size / 5) * 5,//todo: move to remoteConfig
            status = this.status,
        ).apply {
            this.isProcessing = this@StudyNoteWithQuestionsFirestore.isProcessing
            createdAt = this@StudyNoteWithQuestionsFirestore.createdAt
            isPinned = this@StudyNoteWithQuestionsFirestore.isPinned
            isStarred = this@StudyNoteWithQuestionsFirestore.isStarred
            isOpened = this@StudyNoteWithQuestionsFirestore.isOpened
            currentStage = this@StudyNoteWithQuestionsFirestore.currentStage
            nextLevelIn = this@StudyNoteWithQuestionsFirestore.nextLevelIn
            score = this@StudyNoteWithQuestionsFirestore.score
            accuracy = this@StudyNoteWithQuestionsFirestore.accuracy
        }
    }

    fun toQuestionsLocal(): List<Question> {
        return this.questions.map { fq ->
            Question(
//                id = 0,
                statement = fq.sm,
                studyNoteId = this.id,
                options = fq.ops.map { op -> Option(content = op.txt, isCorrect = op.isAns) },
                explanation = fq.ex
            ).apply {
                areaId = fq.areaId
            }
        }
    }

    fun toFlashCardLocal(): List<FlashCard> {
        return this.flashCards.map { fc ->
            FlashCard(
                emoji = fc.emoji, prompt = fc.prompt, info = fc.info, studyNoteId = this.id
            ).apply {
                areaId = fc.areaId
            }
        }
    }
}

private fun List<KeyAreaFirestore>.toLocal(): List<KeyArea> {
    return this.mapIndexed { index, keyAreaFirestore ->
        KeyArea(
            id = index,
            emoji = keyAreaFirestore.emoji,
            name = keyAreaFirestore.name,
            info = keyAreaFirestore.info,

            )
    }
}
