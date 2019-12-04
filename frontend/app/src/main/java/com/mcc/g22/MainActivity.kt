package com.mcc.g22

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.mcc.g22.reportgenerator.ReportPreviewActivity
import java.time.Instant

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enable persistence to be sure that realtime database will be updated
        // It must be done before any other usage of the instance.
        // That's why it is here
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        /*Project.fromProjectId("-LvBqZKJ7x10SS05X5j1", {
            ReportPreviewActivity.startShowingPreview(applicationContext, it)
        }, {
            Log.i("MCC", "Failed to get project")
        })*/

        //NotificationsService.startNotificationService(this)

        val t = Task.createTask("-LvBqZKJ7x10SS05X5j1", "Test crested by call to API",
            Instant.ofEpochSecond(Instant.now().toEpochMilli() / 1000 + 999999))
        t.commitChanges({
            Log.i("MCC", "Task created")
        }, {
            Log.i("MCC", "Creating failed")
        })

        /*Translation.translate("Cześć", "en", {
            Log.i("MCC", it)
        }, {
            Log.i("MCC", "Translation failed")
        })*/
    }
}
