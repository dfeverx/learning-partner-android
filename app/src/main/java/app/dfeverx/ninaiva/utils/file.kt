package app.dfeverx.ninaiva.utils

import android.net.Uri
import java.io.File

object FileUtils {

    /**
     * Returns a File object from the given URI string.
     *
     * @param context The context of the caller.
     * @param uriString The URI string of the file.
     * @return The File object, or null if the file does not exist.
     */
    fun getFileFromUri(uriString: String): File? {
        // Convert the URI string to a URI object
        val uri = Uri.parse(uriString)

        // Get the path from the URI object
        val filePath = uri.path

        // Create a File object
        val file = File(filePath)

        // Check if the file exists
        return if (file.exists()) {
            file
        } else {
            null
        }
    }
}