package app.dfeverx.ninaiva.db.converter

import androidx.room.TypeConverter
import app.dfeverx.ninaiva.models.local.KeyArea
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class KeyAreasConverter {

    @TypeConverter
    fun fromKeyAreaList(keyAreas: List<KeyArea>): String {
        val gson = Gson()
        return gson.toJson(keyAreas)
    }

    @TypeConverter
    fun toKeyAreaList(keyAreaString: String): List<KeyArea> {
        val listType = object : TypeToken<List<KeyArea>>() {}.type
        return Gson().fromJson(keyAreaString, listType)
    }
}