package android.print

import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PrintDocumentAdapter.LayoutResultCallback
import android.print.PrintDocumentAdapter.WriteResultCallback
import android.util.Log
import java.io.File


class PdfPrint(private val printAttributes: PrintAttributes) {

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
                printAdapter.onWrite(
                    arrayOf(PageRange.ALL_PAGES),
                    getOutputFile(path, fileName),
                    CancellationSignal(),
                    object : WriteResultCallback() {
                    })
            }
        }, null)
    }

    private fun getOutputFile(path: File, fileName: String): ParcelFileDescriptor? {
        if (!path.exists()) {
            path.mkdirs()
        }
        val file = File(path, fileName)
        try {
            file.createNewFile()
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open ParcelFileDescriptor", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "MCC"
    }
}
