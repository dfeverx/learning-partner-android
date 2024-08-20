package app.dfeverx.ninaiva.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun ClickableItem(item: ClickableItem, navigateTo: (String) -> Unit) {
    if (item.title == "") {
        return
    }
    Row(
        modifier = Modifier
            .clickable { navigateTo(item.route) }
            .fillMaxWidth()
            .padding(16.dp)
            .padding(horizontal = 16.dp), horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(text = item.title)
        Spacer(modifier = Modifier.weight(1f))
    }
}

class ClickableItem(
    val title: String,
    val icon: ImageVector = Icons.Default.Favorite,
    val route: String
)