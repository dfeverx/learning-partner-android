package app.dfeverx.ninaiva.ui.main

import com.google.android.play.core.appupdate.AppUpdateInfo

class InAppUpdateUiState(
    var isVisible:Boolean= false,
    var appUpdateInfo: AppUpdateInfo? = null,
    var isReadyToInstall: Boolean = false,
    var isDownloading: Boolean = false,
    var progress: Float = 0f
)
