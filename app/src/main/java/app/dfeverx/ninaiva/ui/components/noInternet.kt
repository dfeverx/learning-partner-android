package app.dfeverx.ninaiva.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SignalWifiConnectedNoInternet4
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NoInternetConnectionCard() {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Icon(
            modifier = Modifier.padding(end = 16.dp),
            imageVector = Icons.Outlined.SignalWifiConnectedNoInternet4,
            contentDescription = ""
        )
        Text(text = "Internet connection lost,Please check your Internet connection")

    }
}