package app.dfeverx.ninaiva.db.pre

import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class StudyPartnerDatabaseCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        Log.d("DB", "Database : onCreate  ")
    }
}
