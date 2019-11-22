package com.mcc.g22.reportgenerator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PdfPrint
import android.print.PrintAttributes
import android.print.PrintAttributes.Resolution
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mcc.g22.R
import com.mcc.g22.Task
import com.mcc.g22.User


/**
 * This activity shows a preview of the project report and, after user's confirmation, generate
 * the report.
 */
class ReportPreviewActivity : AppCompatActivity() {

    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_STORAGE: Int = 314

        private lateinit var projectName: String
        private lateinit var projectMembers: Set<User>
        private lateinit var projectTasks: Set<Task>

        fun startShowingPreview(context: Context, projectName: String, projectMembers: Set<User>,
                                projectTasks: Set<Task>) {

            ReportPreviewActivity.projectMembers = projectMembers
            ReportPreviewActivity.projectName = projectName
            ReportPreviewActivity.projectTasks = projectTasks

            val i = Intent(context, ReportPreviewActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_preview)

        // Check if we have storage permission. If not, ask for it
        if (getStoragePermission()) runActivity()
    }

    private fun runActivity() {
        // Set button font
        val generateButton = findViewById<Button>(R.id.generate_report_button)
        val font = Typeface.createFromAsset(assets, "fonts/Montserrat-Bold.ttf")
        generateButton.typeface = font

        // Set information that report is generating and gray button
        generateButton.text = getString(R.string.report_is_loading)
        generateButton.isEnabled = false

        // Generate preview
        val report = StringBuilder()
        report.append("<html>")

        report.append("<head>")
        report.append("<title>$projectName</title>")
        report.append("<style>@font-face {\n" +
                "    font-family: 'Montserrat';\n" +
                "    src: url('fonts/Montserrat-Regular.ttf');\n" +
                "}\nbody {font-family: 'Montserrat';}</style>")
        report.append("</head>")

        report.append("<body>")
        report.append("<h1 align=\"center\">$projectName</h1>")

        report.append("<p align=\"center\"><img style=\"width: 50%;\" src=\"icons/New Project.png\" /></p>")
        report.append("</body>")

        report.append("</html>")

        // Prepare view
        val webView = findViewById<WebView>(R.id.preview_web_view)

        // Set action when the preview is loaded
        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(webView: WebView, url: String) {

                runOnUiThread {
                    generateButton.text = getString(R.string.generate_report_now)
                    generateButton.isEnabled = true
                    generateButton.setOnClickListener { generatePdfReport(webView) }
                }
            }

            override fun onReceivedError(view: WebView?, errorCode: Int, description: String,
                failingUrl: String?
            ) {
                Toast.makeText(applicationContext, "Oh no! $description", Toast.LENGTH_SHORT)
                    .show()
                runOnUiThread {
                    finish()
                }
            }
        }

        // Load preview
        webView.loadDataWithBaseURL("file:///android_asset/",
            report.toString(),
            "text/html",
            "utf-8",
            null)
    }

    private fun generatePdfReport(webView: WebView) {
        val reportFilename = "$projectName-Report.pdf"
        val attributes = PrintAttributes.Builder()
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
            .setResolution(Resolution("pdf", "pdf", 600, 600))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()
        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/TaskManager")
        Log.i("MCC", path.absolutePath)

        if (!path.exists() && !path.mkdirs()) {
            Toast.makeText(applicationContext, R.string.cannot_create_folder, Toast.LENGTH_LONG)
                .show()
            return
        }

        try {
            val pdfPrint = PdfPrint(applicationContext, attributes)
            pdfPrint.print(
                webView.createPrintDocumentAdapter(), // We do this with full responsibility
                // method which is not deprecated was added in API 21
                path,
                reportFilename
            )
        } catch (e: Exception) {
            Toast.makeText(applicationContext, R.string.cannot_save_report, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun requestStoragePermission() {
        // No explanation needed, we can request the permission.
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
            MY_PERMISSIONS_REQUEST_WRITE_STORAGE
        )
    }

    private fun getStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
                alertDialogBuilder.setCancelable(false)
                alertDialogBuilder.setMessage(R.string.storage_permission_expl)
                alertDialogBuilder.setPositiveButton("OK"
                ) { _, _ -> requestStoragePermission() }
            } else {
                requestStoragePermission()
            }
            return true
        } else {
            // Permission already granted
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    runActivity()
                } else {
                    // permission denied, boo!
                    Toast.makeText(applicationContext, R.string.storage_permission_needed,
                        Toast.LENGTH_LONG).show()
                    finish()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

}
