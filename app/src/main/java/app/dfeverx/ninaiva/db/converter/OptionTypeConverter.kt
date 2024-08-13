package app.dfeverx.ninaiva.db.converter

import androidx.room.TypeConverter
import app.dfeverx.ninaiva.models.local.Option
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class OptionTypeConverter {

    @TypeConverter
    fun fromString(value: String): List<Option> {
        return Gson().fromJson(value, object : TypeToken<List<Option>>() {}.type)
    }

    @TypeConverter
    fun toString(options: List<Option>): String {
        return Gson().toJson(options)
    }
}