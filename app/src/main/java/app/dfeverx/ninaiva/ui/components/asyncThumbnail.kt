package app.dfeverx.ninaiva.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import app.dfeverx.ninaiva.R
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size.Companion.ORIGINAL

@Composable
fun AsyncThumbnail(
    modifier: Modifier = Modifier.fillMaxWidth(),
    url: Any,
    contentDescription: String,
    contentScale: ContentScale = ContentScale.FillWidth
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(R.drawable.grid)
            .size(ORIGINAL)
            .build()
    )

    AsyncImage(
        modifier = modifier.paint(
            painter = painter,
            contentScale = ContentScale.FillBounds
        ),
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        placeholder = painterResource(id = R.drawable.image_placeholder),
        error = painterResource(id = R.drawable.image_placeholder)
    )


}