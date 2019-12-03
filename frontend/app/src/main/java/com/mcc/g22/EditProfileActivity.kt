package com.mcc.g22

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_dashboard.*

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
    }

    fun toggleDrawer(view: View){
        if(drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }
}
