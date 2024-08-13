package app.dfeverx.ninaiva.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun TextProgressButton(
    modifier: Modifier,
    isLoading: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    TextButton(
        modifier = modifier, enabled = !isLoading,
        onClick = { onClick() }) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        } else {
            content()
        }
    }
}