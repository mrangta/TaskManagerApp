package android.print

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PrintDocumentAdapter.LayoutResultCallback
import android.print.PrintDocumentAdapter.WriteResultCallback
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.mcc.g22.NotificationHelper
import com.mcc.g22.R
import java.io.File


class PdfPrint(private val context: Context, private val printAttributes: PrintAttributes) {
    private val notificationHelper: NotificationHelper =
        NotificationHelper(context, "Report Generator",
            "Report Generator", 3)

    fun print(
        printAdapter: PrintDocumentAdapter,
        path: File,
        fileName: String
    ) {
        printAdapter.onLayout(null, printAttributes, null, object : LayoutResultCallback() {
            override fun onLayoutFinished(
                info: PrintDocumentInfo,
                changed: Boolean
            ) {
                val outputFile = getOutputFile(path, fileName)
                printAdapter.onWrite(
                    arrayOf(PageRange.ALL_PAGES),
                    ParcelFileDescriptor.open(outputFile, ParcelFileDescriptor.MODE_READ_WRITE),
                    CancellationSignal(),
                    object : WriteResultCallback() {
                        override fun onWriteFailed(error: CharSequence) {
                            notificationHelper.showNotification(
                                context.getString(R.string.generation_failed_title),
                                context.getString(R.string.generation_failed_text) + error)
                        }

                        override fun onWriteCancelled() {
                            notificationHelper.showNotification(
                                context.getString(R.string.generation_cancelled_title),
                                "")
                        }

                        override fun onWriteFinished(pages: Array<PageRange>) {

                            val toLaunch = Intent()
                            toLaunch.action = Intent.ACTION_VIEW
                            toLaunch.setDataAndType(
                                FileProvider.getUriForFile(context,
                                    context.packageName + ".fileprovider",
                                    outputFile),
                                "application/pdf"
                            )
                            toLaunch.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                            val contentIntent =
                                PendingIntent.getActivity(context, 0, toLaunch, 0)

                            notificationHelper.showNotification(
                                context.getString(R.string.generation_finished_title),
                                context.getString(R.string.generation_finished_text),
                                onTapAction = contentIntent)
                        }
                    })
            }
        }, null)
    }

    private fun getOutputFile(path: File, fileName: String): File {
        if (!path.exists()) {
            path.mkdirs()
        }
        val file = File(path, fileName)
        try {
            file.createNewFile()
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open ParcelFileDescriptor", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "MCC"
    }
}
