package com.mcc.g22


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat.*
import kotlinx.android.synthetic.main.activity_dashboard.*
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView


class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        nav_view.setNavigationItemSelectedListener(this)
    }

    fun toggleDrawer(view: View){
        if(drawer_layout.isDrawerOpen(START)) {
            drawer_layout.closeDrawer(START)
        }
        else {
            drawer_layout.openDrawer(START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.getItemId()) {
            R.id.nav_profile -> {
                showProfile()
                return true
            }
            R.id.nav_notifications -> {
                showNotifications()
                return true
            }
            R.id.nav_projects -> {
                showCreatedProjects()
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

    fun showNotifications() {

    }

    fun showCreatedProjects() {

    }

    fun logOut() {

    }

   


}
