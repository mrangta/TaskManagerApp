package com.mcc.g22

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_my_tasks.*

class MyTasksActivity : AppCompatActivity() {

    var array = arrayOf("Create backend for the project", "Write documentation")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tasks)

        val adapter = ArrayAdapter(this,
            R.layout.task, array)

        ongoingList.setAdapter(adapter)
        pendingList.setAdapter(adapter)
        completedList.setAdapter(adapter)
    }
}
