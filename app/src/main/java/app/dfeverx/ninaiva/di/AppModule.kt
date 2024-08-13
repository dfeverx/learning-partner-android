package app.dfeverx.ninaiva.di


import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import app.dfeverx.ninaiva.LearningPartnerApplication
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.datastore.CreditAndSubscriptionDataStore
import app.dfeverx.ninaiva.datastore.StreakDataStore
import app.dfeverx.ninaiva.db.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ocpsoft.prettytime.PrettyTime
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val DATABASE_NAME = "study_partner_db"

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext app: Context): LearningPartnerApplication {
        return app as LearningPartnerApplication

    }

    @Singleton
    @Provides
    fun provideAlarmManager(@ApplicationContext app: Context): AlarmManager {
        return app.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    }


    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    @Provides
    @Singleton
    fun provideAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideStorage(): FirebaseStorage {
        return Firebase.storage
    }

    @Provides
    @Singleton
    fun provideFunction(): FirebaseFunctions {
        return Firebase.functions
    }

    /*   @Singleton
       @Provides
       fun provideGso(@ApplicationContext app: Context): GoogleSignInOptions {
           return GoogleSignInOptions
               .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
               .requestIdToken(ContextCompat.getString(app, R.string.web_client_id))
               .requestEmail().build()

       }*/


    @Module
    @InstallIn(SingletonComponent::class)
    class AppModule {
        @Singleton
        @Provides
        fun provideContext(application: Application): Context = application.applicationContext
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context, sharedPreferences: SharedPreferences
    ): AppDatabase {
        return try {
            AppDatabase.getInstance(context, DATABASE_NAME)
        } catch (e: Exception) {
//            marked as initial fetch
            sharedPreferences.getBoolean(IS_INITIAL_NOTES_FETCH, true)
            AppDatabase.getInstance(context, DATABASE_NAME)

        }
    }


    @Provides
    @Singleton
    fun provideStreakDataStore(
        @ApplicationContext context: Context,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): StreakDataStore {
        return StreakDataStore(context, firestore, auth)
    }


    @Provides
    @Singleton
    fun provideCreditDataStore(
        @ApplicationContext context: Context,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): CreditAndSubscriptionDataStore {
        return CreditAndSubscriptionDataStore(context, firestore, auth)
    }

    @Provides
    @Singleton
    fun providePrettyTime(): PrettyTime {
        return PrettyTime()
    }

    @Provides
    @Singleton
    fun provideRemoteConfig(application: Application): FirebaseRemoteConfig {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
//            minimumFetchIntervalInSeconds = 3600

        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
        return remoteConfig
    }


    @Provides
    @Singleton
    fun provideSharedPref(application: Application): SharedPreferences {
        return application.getSharedPreferences("app.ninaiva.shared_pref", Context.MODE_PRIVATE)
    }

}

const val IS_INITIAL_NOTES_FETCH = "initial_fetch_notes"






