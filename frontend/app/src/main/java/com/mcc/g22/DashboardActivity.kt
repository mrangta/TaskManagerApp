package com.mcc.g22

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat.*
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

    }

    fun toggleDrawer(view: View){
        if(drawer_layout.isDrawerOpen(START)) {
            drawer_layout.closeDrawer(START)
        }
        else {
            drawer_layout.openDrawer(START)
        }
    }


}
