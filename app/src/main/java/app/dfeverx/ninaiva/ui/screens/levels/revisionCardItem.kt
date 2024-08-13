package app.dfeverx.ninaiva.ui.screens.levels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage


@Composable
fun RevisionCardItem(isPlayable: Boolean = false, onClickPlay: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()

    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AsyncImage(model = "", contentDescription = "")
            Text(
                modifier = Modifier,
                text = "Revision 1",
                style = MaterialTheme.typography.titleLarge
            )
            Text(modifier = Modifier.padding(bottom = 8.dp), text = "Revision 1")
            if (isPlayable) {
                FilledTonalButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Outlined.PlayArrow, contentDescription = "")
                    Text(text = "Play now")
                }
            } else {

                FilledTonalButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Outlined.Lock, contentDescription = "")
                    Text(text = "Locked")
                }
            }
        }
    }
}