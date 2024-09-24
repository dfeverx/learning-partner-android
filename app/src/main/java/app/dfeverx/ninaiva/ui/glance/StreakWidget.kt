package app.dfeverx.ninaiva.ui.glance

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import app.dfeverx.ninaiva.R
import app.dfeverx.ninaiva.datastore.StreakDataStore
import app.dfeverx.ninaiva.datastore.StreakInfo
import app.dfeverx.ninaiva.ui.main.MainActivity
import app.dfeverx.ninaiva.utils.relativeTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext


class StreakWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val streakInfo = withContext(Dispatchers.IO) {
            return@withContext StreakDataStore.getInstance(context).streakInfoFlow.first()
        }
        provideContent {

            GlanceTheme(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    GlanceTheme.colors
                else
                    MyAppWidgetGlanceColorScheme.colors
            ) {


                StreakWidgetUi(streakInfo)
            }
        }
    }

    companion object {
        // Helper method to trigger widget update manually
        suspend fun updateWidget(context: Context) {
            val glanceId =
                GlanceAppWidgetManager(context).getGlanceIds(StreakWidget::class.java).firstOrNull()
            glanceId?.let {
                StreakWidget().update(context, glanceId)
            }
        }
    }
}

@Composable
private fun StreakWidgetUi(streakInfo: StreakInfo) {
    Scaffold(
        backgroundColor = GlanceTheme.colors.surface,
        modifier = GlanceModifier.cornerRadius(8.dp)
            .clickable(onClick = actionStartActivity<MainActivity>()),

        ) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
//                .background(GlanceTheme.colors.background)
            ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_streak),
                contentDescription = "",
                modifier = GlanceModifier.size(58.dp)
            )

            Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    Text(
                        text = streakInfo.count.toString(),
                        modifier = GlanceModifier.padding(horizontal = 12.dp),
                        style = TextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Streaks",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium
                        ),
                    )
                }
                Text(
                    text = "From ${streakInfo.from.relativeTime()}",
                    modifier = GlanceModifier.padding(start = 12.dp),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 16.sp
                    )
                )
                /*  Button(
                      text = "Home",
                      onClick = actionStartActivity<MainActivity>()
                  )
                  Button(
                      text = "Work",
                      onClick = actionStartActivity<MainActivity>()
                  )*/
            }
            Spacer(modifier = GlanceModifier.defaultWeight())
            if (false) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier
                ) {
                    Text(
                        text = "3",
                        modifier = GlanceModifier.background(
                            imageProvider = ImageProvider(R.drawable.circle),
                            contentScale = ContentScale.FillBounds
                        )
                            .padding(4.dp).size(42.dp), style = TextStyle(
                            color = GlanceTheme.colors.onBackground,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium, textAlign = TextAlign.Center
                        )
                    )
                    Text(text = "Pending", style = TextStyle(color = GlanceTheme.colors.onSurface))
                }
            }
        }
    }

}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { preferences ->
            preferences.toMutablePreferences().apply {
//                this[currentTextKey] = parameters.getOrDefault("quote", "")
            }
        }
        StreakWidget().update(context, glanceId)
    }
}

