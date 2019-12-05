package com.mcc.g22

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.PopupMenu
import kotlinx.android.synthetic.main.activity_create_task.*

class CreateTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        task_status.setOnClickListener {
            val popupMenu: PopupMenu = PopupMenu(this, task_status)
            popupMenu.menuInflater.inflate(R.menu.task_status, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.ongoing ->
                        task_status.text = "Ongoing"
                    R.id.pending ->
                        task_status.text = "Pending"
                    R.id.completed ->
                        task_status.text = "Completed"
                }
                true
            })
            popupMenu.show()
        }
    }
}
