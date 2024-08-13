package app.dfeverx.ninaiva.ui.screens.doc

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.rajat.pdfviewer.compose.PdfRendererViewCompose


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocViewer(navController: NavController) {

    val docViewModel = hiltViewModel<DocViewerViewModel>()
    val docFile by docViewModel.docFile.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        "Go back to previous",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .clickable {
                                navController.navigateUp()
                            }
                            .padding(8.dp),
                    )
                },
                title = {

                },
            )
        },

        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                when (docFile) {
                    FileState.Error -> {
//                        todo :better error ui
                        Text(text = "Document failed to download")
                        Button(onClick = { docViewModel.retryDocFetch() }) {
                            Text(text = "Retry now")
                        }
                    }

                    is FileState.Progress -> {
                        CircularProgressIndicator()
                        (docFile as FileState.Progress).progress?.let {
                            Text(
                                modifier = Modifier.padding(16.dp),
                                text = it.times(100).toInt()
                                    .toString() + "%"
                            )
                        }
                    }

                    is FileState.Success -> {
                        PdfRendererViewCompose(
                            modifier = Modifier.fillMaxSize(),
                            //url = "https://www.adobe.com/support/products/enterprise/knowledgecenter/media/c4611_sample_explain.pdf",
                            file = (docFile as FileState.Success).file,
                            lifecycleOwner = LocalLifecycleOwner.current,
                        )
                    }
                }


            }

        })

}


