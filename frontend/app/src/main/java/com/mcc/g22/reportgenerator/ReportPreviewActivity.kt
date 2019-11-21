package com.mcc.g22.reportgenerator

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.print.PdfPrint
import android.print.PrintAttributes
import android.print.PrintAttributes.Resolution
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mcc.g22.R
import com.mcc.g22.Task
import com.mcc.g22.User
import java.io.File


/**
 * This activity shows a preview of the project report and, after user's confirmation, generate
 * the report.
 */
class ReportPreviewActivity : AppCompatActivity() {

    companion object {
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
        val path = File(filesDir.absolutePath + "/TaskManager")
        Log.i("MCC", path.absolutePath)

        if (!path.mkdirs()) {
            Toast.makeText(applicationContext, R.string.cannot_create_folder, Toast.LENGTH_LONG)
                .show()
            return
        }

        try {
            val pdfPrint = PdfPrint(attributes)
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
}
