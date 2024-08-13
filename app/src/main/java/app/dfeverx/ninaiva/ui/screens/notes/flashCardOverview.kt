package app.dfeverx.ninaiva.ui.screens.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.dfeverx.ninaiva.models.local.FlashCard


@Composable
fun FlashCardOverview(info: FlashCard, onClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier.padding(8.dp),
            shape = CircleShape,
            onClick = { onClick() }, border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = info.emoji,
                style = MaterialTheme.typography.headlineMedium
            )

        }
        Text(
            modifier = Modifier.widthIn(max = 72.dp),
            text = info.prompt,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1, style = MaterialTheme.typography.bodySmall
        )
    }

}

@Preview

@Composable
fun PreviewFlashCardOverview() {
    FlashCardOverview(FlashCard(emoji = "✅", prompt = "Laws of motions"), {

    })
}