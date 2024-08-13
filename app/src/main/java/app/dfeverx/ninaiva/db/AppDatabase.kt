package app.dfeverx.ninaiva.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.dfeverx.ninaiva.db.converter.KeyAreasConverter
import app.dfeverx.ninaiva.db.converter.LongListConverter
import app.dfeverx.ninaiva.db.converter.MarkdownStringListConverter
import app.dfeverx.ninaiva.db.converter.OptionTypeConverter
import app.dfeverx.ninaiva.db.dao.FlashDao
import app.dfeverx.ninaiva.db.dao.LevelDao
import app.dfeverx.ninaiva.db.dao.QuestionDao
import app.dfeverx.ninaiva.db.dao.StudyNoteDao
import app.dfeverx.ninaiva.db.pre.StudyPartnerDatabaseCallback
import app.dfeverx.ninaiva.models.local.FlashCard
import app.dfeverx.ninaiva.models.local.Level
import app.dfeverx.ninaiva.models.local.Question
import app.dfeverx.ninaiva.models.local.StudyNote

@Database(
    entities = [
        StudyNote::class,
        Question::class,
        FlashCard::class,
        Level::class
    ],
    version = 11,
)
@TypeConverters(
    MarkdownStringListConverter::class,
    OptionTypeConverter::class,
    KeyAreasConverter::class,
    LongListConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studyNotesDao(): StudyNoteDao
    abstract fun questionDao(): QuestionDao
    abstract fun levelDao(): LevelDao
    abstract fun flashDao(): FlashDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, dbName: String): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        dbName
                    )
                        .addCallback(StudyPartnerDatabaseCallback())
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }


}