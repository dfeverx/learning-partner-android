package app.dfeverx.ninaiva.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.dfeverx.ninaiva.models.local.FlashCard
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer


/*@Composable
fun FlashCardOverview(info: FlashCard, onClick: () -> Unit) {

    val isPlaceholder: Boolean = info.isPlaceholder

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

}*/

@Composable
fun FlashCardOverview(info: FlashCard, onClick: () -> Unit) {

    val isPlaceholder: Boolean = info.isPlaceholder
    val shimmerInstance = rememberShimmer(shimmerBounds = ShimmerBounds.Window)

    Column(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .then(if (isPlaceholder) Modifier.shimmer(shimmerInstance) else Modifier),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier
                .padding(8.dp)
                .then(if (isPlaceholder) Modifier/*.shimmer(shimmerInstance)*/ else Modifier),
            shape = CircleShape,
            onClick = { if (!isPlaceholder) onClick() },
            border = if (!isPlaceholder) BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            ) else null
        ) {

            Text(
                modifier = Modifier
                    .padding(16.dp)
                    .then(
                        if (isPlaceholder) Modifier.alpha(0f) else Modifier.alpha(1f)
                    ),
                text = info.emoji,
                style = MaterialTheme.typography.headlineMedium
            )

        }

        Text(
            modifier = Modifier
                .widthIn(max = 72.dp)
                .then(
                    if (isPlaceholder) Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .width(76.dp)
                        /*.shimmer(shimmerInstance)*/
                        .background(MaterialTheme.colorScheme.surfaceVariant) else Modifier
                ),
            text = if (isPlaceholder) "" else info.prompt,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Preview

@Composable
fun PreviewFlashCardOverview() {
    FlashCardOverview(FlashCard(emoji = "✅", prompt = "Laws of motions"), {

    })
}

@Preview

@Composable
fun PreviewFlashCardOverviewPlaceHolder() {
    FlashCardOverview(FlashCard(emoji = "-----", prompt = "Laws of motions").apply {
        isPlaceholder = true
    }, {

    })
}