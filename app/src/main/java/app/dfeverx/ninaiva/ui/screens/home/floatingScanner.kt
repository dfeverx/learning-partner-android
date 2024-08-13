package app.dfeverx.ninaiva.ui.screens.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun FloatingScannerButton(expanded: Boolean, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        modifier = Modifier
            .padding(end = 4.dp)
            .height(72.dp)
            .widthIn(min = 72.dp)
            .animateContentSize(),
        expanded = expanded,
        onClick = {
            onClick()
        },
        icon = {
            Icon(
                Icons.Outlined.DocumentScanner,
                "Extended floating action button."
            )
        },
        text = { Text(text = "Scan notes") },

        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp
        )
    )
}