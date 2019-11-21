package com.mcc.g22

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mcc.g22.reportgenerator.ReportPreviewActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ReportPreviewActivity.startShowingPreview(applicationContext,
            "test project",
            setOf<User>(User.getRegisteredUser()!!),
            setOf<Task>(
                Task.createTask("projId",
                    "description of task",
                    Date(),
                    mutableSetOf<User>(User.getRegisteredUser()!!)),
                Task.createTask("projId",
                    "description of task, but task with later deadline",
                    Date(System.currentTimeMillis() + 6000000),
                    mutableSetOf<User>(User.getRegisteredUser()!!))))
    }
}
