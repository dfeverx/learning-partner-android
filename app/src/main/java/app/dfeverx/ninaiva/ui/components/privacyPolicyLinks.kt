package app.dfeverx.ninaiva.ui.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
 fun PrivacyPolicyLinks(activity: Activity) {
    Text(
        text = "Privacy policy | Terms ",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                try {
                    activity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://dfeverx.com/ninaiva-learning-partner/privacy-policy/")
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    activity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://dfeverx.com/ninaiva-learning-partner/privacy-policy/")
                        )
                    )
                }
            },
        textAlign = TextAlign.Center
//                modifier = Modifier.align(Alignment.CenterHorizontally)
    )
}