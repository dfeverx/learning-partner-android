package app.dfeverx.ninaiva.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.dfeverx.ninaiva.R
import coil.compose.AsyncImage


@Composable
fun ModernGrid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {


    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 164.dp), contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Transparent),
            model = R.drawable.grid,
            contentDescription = "",
            contentScale = ContentScale.FillWidth
        )
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(16.dp)
//                .clickable { onClick() }
                .shadow(32.dp, shape = CircleShape)
                .background(color = MaterialTheme.colorScheme.surface, shape = CircleShape)
                .fillMaxSize()
                .padding(16.dp), contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}


@Preview
@Composable
fun PreviewGrid() {
//    GridCircleIcon()
}