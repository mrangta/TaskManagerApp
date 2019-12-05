package com.mcc.g22

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.widget.PopupMenu
import kotlinx.android.synthetic.main.activity_create_project.*
import kotlinx.android.synthetic.main.activity_create_task.*
import kotlinx.android.synthetic.main.activity_my_tasks.*

class CreateProjectActivity : AppCompatActivity() {

    var arrayList = ArrayList<String>()

    private lateinit var adapter : ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_project)

        project_type.setOnClickListener {
            val popupMenu: PopupMenu = PopupMenu(this, project_type)
            popupMenu.menuInflater.inflate(R.menu.project_type, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.personal ->
                        project_type.text = "Personal"
                    R.id.group ->
                        project_type.text = "Group"
                }
                true
            })
            popupMenu.show()
        }

        adapter = ArrayAdapter(this,
            R.layout.keyword, arrayList)

        keyword_list.adapter = adapter

        project_keywords.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.action == KeyEvent.ACTION_UP) {
                    arrayList.add(project_keywords.text.toString())
                    adapter.notifyDataSetChanged()
                    return@OnKeyListener true
                }
            }
            false
        })
    }

}
