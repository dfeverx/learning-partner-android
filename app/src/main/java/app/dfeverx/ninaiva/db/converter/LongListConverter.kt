package app.dfeverx.ninaiva.db.converter

import androidx.room.TypeConverter

class LongListConverter {
    @TypeConverter
    fun fromQuestionIds(questionIds: List<Long>): String {
        return questionIds.joinToString(separator = ",")
    }

    @TypeConverter
    fun toQuestionIds(questionIdsString: String): List<Long> {
        return questionIdsString.split(",").map { it.toLong() }
    }
}