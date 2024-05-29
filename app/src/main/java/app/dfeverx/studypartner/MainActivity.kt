package app.dfeverx.studypartner

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import app.dfeverx.studypartner.ui.theme.StudyPartnerTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(2)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()


        setContent {
            StudyPartnerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    DocumentScanner(options)
                }
            }
        }
    }
}

@Composable
fun DocumentScanner(options: GmsDocumentScannerOptions) {
    val activity = LocalContext.current as ComponentActivity
    val scanner = GmsDocumentScanning.getClient(options)
    // When using Latin script library
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val documentScanningResult =
                GmsDocumentScanningResult.fromActivityResultIntent(
                    result.data
                )
            documentScanningResult?.pages?.let { pages ->
                for (page in pages) {
                    val imageUri = page.imageUri
                    Log.d("TAG", "DocumentScanner: $imageUri")
                    val image: InputImage
                    try {
                        image = InputImage.fromFilePath(activity, imageUri)
                        recognizer.process(image)
                            .addOnSuccessListener { visionText ->
                                // Task completed successfully
                                // ...
                                Log.d("TAG", "DocumentScanner: text ${visionText.text}")
                            }
                            .addOnFailureListener { e ->
                                // Task failed with an exception
                                // ...
                                Log.d("TAG", "DocumentScanner: error text ${e.message}")
                            }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    // Handle the image URI
                }
            }
            documentScanningResult?.pdf?.let { pdf ->
                val pdfUri = pdf.uri
                val pageCount = pdf.pageCount
                Log.d("TAG", "DocumentScanner: $pdfUri")
                // Handle the PDF URI and page count
            }
        }
    }

    Button(onClick = {

        scanner.getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(
                    IntentSenderRequest.Builder(intentSender).build()
                )
            }.addOnFailureListener {
                Log.d("TAG", "DocumentScanner: Error :${it.message}")
            }
    }) {
        Text("Start Document Scanner")
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StudyPartnerTheme {
        Greeting("Android")
    }
}