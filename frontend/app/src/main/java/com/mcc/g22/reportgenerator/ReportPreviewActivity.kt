package com.mcc.g22.reportgenerator

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.LocusId
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mcc.g22.Project
import com.mcc.g22.R
import com.mcc.g22.Task
import com.mcc.g22.User
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread


/**
 * This activity shows a preview of the project report and, after user's confirmation, generate
 * the report.
 */
class ReportPreviewActivity : AppCompatActivity() {
    private lateinit var errorMsgDialog: AlertDialog
    private val report = StringBuilder()
    private lateinit var generateButton: Button

    private class Event {
        lateinit var task: Task
        var eventId: String = ""
        var eventType: String = ""
        var description: String = ""
        var timestamp: String = ""
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_STORAGE: Int = 314
        private const val MAX_DESCRIPTION_SIZE: Int = 30

        private lateinit var project: Project

        fun startShowingPreview(context: Context, project: Project) {

            ReportPreviewActivity.project = project

            val i = Intent(context, ReportPreviewActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_preview)

        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setMessage(R.string.error_in_generating_report)
        builder.setNeutralButton(R.string.ok) { dialog, which -> ;}
        errorMsgDialog = builder.create()

        // Check if we have storage permission. If not, ask for it
        if (getStoragePermission()) startGeneratingReport()
    }

    private fun runActivity(displayNamesOfMembers: Set<String>) {
        // Set button font
        generateButton = findViewById(R.id.generate_report_button)
        val font = Typeface.createFromAsset(assets, "fonts/Montserrat-Bold.ttf")
        generateButton.typeface = font

        // Set information that report is generating and gray button
        generateButton.text = getString(R.string.report_is_loading)
        generateButton.isEnabled = false

        // Generate preview
        report.append("<html>")

        report.append("<head>")
        report.append("<title>${project.name}</title>")
        report.append(
            "<style>@font-face {\n" +
                    "    font-family: 'Montserrat';\n" +
                    "    src: url('fonts/Montserrat-Regular.ttf');\n" +
                    "}\nbody {font-family: 'Montserrat';}</style>"
        )
        report.append("</head>")

        report.append("<body>")
        report.append("<h1 align=\"center\">${project.name}</h1>")

        report.append("<p><h3>Members</h3>")
        report.append("<ul>")
        for (u in displayNamesOfMembers) {
            report.append("<li>$u</li>")
        }
        report.append("</ul></p>")

        report.append("<p><h3>Tasks</h3>")
        report.append("<ul>")

        // Get logs from the database
        val logRef = FirebaseDatabase.getInstance().reference
            .child("log").child(project.projectId)
        logRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(databaseError: DatabaseError) {
                errorOccurred()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val projectEvents = mutableSetOf<Event>()
                val latch = CountDownLatch(dataSnapshot.childrenCount.toInt())

                for (task in dataSnapshot.children) {

                    Task.getTaskFromDatabase(task.key as String,
                        {
                            for (event in task.children) {

                                val e = Event()
                                e.task = it
                                e.eventId = event.key as String
                                e.description = event.child("description").value as String
                                e.eventType = event.child("eventType").value as String
                                e.timestamp = event.child("timestamp").value as String
                                projectEvents.add(e)
                            }
                            latch.countDown()
                        }, {
                            errorOccurred()
                            latch.countDown()
                        })
                }

                thread {
                    latch.await() // We can wait because this function is called
                    // in a separate thread so it won't block UI

                    val sortedEvents = projectEvents.toSortedSet(Comparator { o1, o2 ->
                        when {
                            o1!!.timestamp > o2!!.timestamp -> {
                                -1
                            }
                            o1.timestamp < o2.timestamp -> {
                                1
                            }
                            else -> 0
                        }
                    })
                    for (t in sortedEvents) {
                        var title = t.task.description
                        if (title.length > MAX_DESCRIPTION_SIZE) {
                            title = title.substring(0, MAX_DESCRIPTION_SIZE) + "..."
                        }
                        report.append("<li><b>" + title + "</b><br />" + t.description + "</li>")
                    }

                    runOnUiThread { finishAndLoad() }
                }
            }
        })
    }

    private fun finishAndLoad() {
        report.append("</ul></p>")

        report.append("</br><p align=\"center\"><img style=\"width: 25%;\" src=\"icons/New Project.png\" /></p>")
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
        val reportFilename = "${project.name}-Report.pdf"
        val attributes = PrintAttributes.Builder()
            .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4.asPortrait())
            .setResolution(Resolution("pdf", "pdf", 600, 600))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS).build()
        val path =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/TaskManager")

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
                    startGeneratingReport()
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

    private fun startGeneratingReport() {
        val displayNamesOfMembers = mutableSetOf<String>()
        for (m in project.membersIds) {
            User.resolveDisplayName(m, {
                displayNamesOfMembers.add(it)

                if (displayNamesOfMembers.size == project.membersIds.size) {
                    runOnUiThread { runActivity(displayNamesOfMembers) }
                }
            }, {
                errorOccurred()
                return@resolveDisplayName
            })
        }
    }

    private fun errorOccurred() {
        runOnUiThread {
            generateButton.text = getString(R.string.error)
            errorMsgDialog.show()
        }
    }
}
