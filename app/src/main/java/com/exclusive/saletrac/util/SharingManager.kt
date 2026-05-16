package com.exclusive.saletrac.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object SharingManager {

    /**
     * Shares a PDF file using Android's native Share Sheet.
     * The file is expected to be in the internal cache directory.
     */
    fun shareReport(context: Context, pdfFile: File) {
        if (!pdfFile.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "M/S EXCLUSIVE Sales Report")
            putExtra(Intent.EXTRA_TEXT, "Please find the attached sales report.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // WhatsApp specific package intent can be forced, but ACTION_SEND allows user choice.
        // If strictly WhatsApp is needed: intent.setPackage("com.whatsapp")

        val chooser = Intent.createChooser(intent, "Share Report via...")

        // We use context.startActivity. To delete the file after sharing,
        // we ideally need to know when the intent finishes.
        // A simple approach is scheduling a delayed cleanup, or letting the OS manage cache.
        // Here we start the activity and delete the file on the next app startup,
        // or immediately after the chooser is dismissed (though exact timing is tricky without ActivityResultLauncher).
        // Let's implement a rudimentary cleanup mechanism that deletes old files before creating a new one.

        context.startActivity(chooser)
    }

    /**
     * Deletes all old reports in the cache to save space and prevent leaks.
     */
    fun cleanupOldReports(context: Context) {
        val reportsDir = File(context.cacheDir, "reports")
        if (reportsDir.exists() && reportsDir.isDirectory) {
            reportsDir.listFiles()?.forEach { file ->
                file.delete()
            }
        }
    }
}
