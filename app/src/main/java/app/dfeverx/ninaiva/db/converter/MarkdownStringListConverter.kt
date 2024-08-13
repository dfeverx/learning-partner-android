package app.dfeverx.ninaiva.db.converter

import android.util.Base64
import androidx.room.TypeConverter

class MarkdownStringListConverter {

    @TypeConverter
    fun fromMarkdownArray(value: List<String>): String {
        val b64EM = value.map { markdownString ->
            Base64.encodeToString(
                markdownString.toByteArray(),
                Base64.DEFAULT
            )

        }
        return b64EM.joinToString(",")

    }

    @TypeConverter
    fun fromString(value: String): List<String> {
        val b64EM = value.split(",").toTypedArray()
        return b64EM.map { bm -> String(Base64.decode(bm, Base64.DEFAULT)) }

    }
}