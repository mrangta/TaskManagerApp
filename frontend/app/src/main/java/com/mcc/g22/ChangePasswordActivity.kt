package com.mcc.g22

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_edit_profile.*

class ChangePasswordActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.getItemId()) {
            R.id.nav_profile -> {
                showProfile()
                return true
            }
            R.id.nav_changePassword -> {
                changePassword()
                return true
            }
            R.id.nav_logOut -> {
                logOut()
                return true
            }

        }
        return true
    }

    fun showProfile() {
        intent = Intent(this, EditProfileActivity::class.java)
        startActivity(intent)
    }


    fun changePassword() {
        intent = Intent(this, ChangePasswordActivity::class.java)
        startActivity(intent)
    }

    fun logOut() {

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

    }

    fun returnHome(view: View) {
        intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }
}
