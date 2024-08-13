package app.dfeverx.ninaiva.ui.screens.home.update

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DownloadDone
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.dfeverx.ninaiva.ui.main.InAppUpdateUiState
import app.dfeverx.ninaiva.ui.main.MainActivity
import app.dfeverx.ninaiva.ui.main.MainActivity.Companion.startUpdate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun InAppUpdateCard(
    modifier: Modifier = Modifier,
    state: InAppUpdateUiState,
) {
    val activity = LocalContext.current as Activity
    val coScope = rememberCoroutineScope()
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                modifier = Modifier.size(36.dp),
                imageVector = if (state.isReadyToInstall) Icons.Outlined.DownloadDone else Icons.Outlined.Download,
                contentDescription = ""
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                val text = when {
                    state.isReadyToInstall -> "Update ready to install"
                    state.isDownloading -> "Downloading.."
                    else -> "New version available"
                }
                Text(
                    modifier = Modifier.padding(vertical = 4.dp),
                    text = text,
                    style = MaterialTheme.typography.labelMedium
                )

                if (!state.isDownloading) {
                    val subText = if (state.isReadyToInstall) {
                        "Simply click update now"
                    } else {
                        "Get better version of the app"
                    }
                    Text(
                        modifier = Modifier,
                        text = subText,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = state.progress
                    )
                }
            }

            if (!state.isDownloading) {
                Button(onClick = {
                    coScope.launch(Dispatchers.Main) {
                        if (state.isReadyToInstall) MainActivity.installUpdate() else state.appUpdateInfo?.let {
                            activity.startUpdate(
                                it
                            )
                        }
                    }

                }) {
                    Text(text = if (state.isReadyToInstall) "Update now" else "Download now")
                }
            }
        }
    }
}


@Preview

@Composable
fun PreviewUpdate() {
    InAppUpdateCard(
        state = InAppUpdateUiState(
            isReadyToInstall = false,
            isDownloading = true,
            progress = .7f
        )
    )
}