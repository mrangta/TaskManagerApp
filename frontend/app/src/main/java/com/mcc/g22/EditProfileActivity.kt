package com.mcc.g22

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_dashboard.drawer_layout
import kotlinx.android.synthetic.main.activity_edit_profile.*

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

    fun changePassword(view: View) {
        currentPassword.visibility = VISIBLE
        newPassword.visibility = VISIBLE
        confirmNewPassword.visibility = VISIBLE
        update.visibility = VISIBLE
    }

    fun updatePassword(view: View) {

    }

    fun returnHome(view: View) {
        intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }
}
