package app.dfeverx.ninaiva.ui.screens.levels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.dfeverx.ninaiva.R
import coil.compose.AsyncImage


@Composable
fun RandomPlayCard(modifier: Modifier = Modifier, handlePractice: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)) {
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = "Practice",
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .padding(top = 4.dp),
                    text = "Want to practice about this subject try this now"
                )
                Button(modifier = Modifier.padding(vertical = 8.dp), onClick = { handlePractice() }) {
                    Text(text = "Practice now")
                }
            }
            AsyncImage(
                modifier = Modifier.size(72.dp),
                model = R.drawable.ic_launcher_foreground,
                contentDescription = ""
            )

        }
    }

}