package com.mcc.g22

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.VISIBLE
import androidx.core.view.GravityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_dashboard.drawer_layout
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_edit_profile.nav_view

class EditProfileActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        nav_view.setNavigationItemSelectedListener(this)
        bottom_nav_view.setOnNavigationItemSelectedListener(this)
    }

    fun toggleDrawer(view: View){
        if(drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.getItemId()) {
            R.id.nav_profile -> {
                showProfile()
                return true
            }
            R.id.nav_logOut -> {
                logOut()
                return true
            }
            R.id.nav_home -> {
                returnHome()
            }
            R.id.nav_fav -> {
                myFavorites()
            }
            R.id.nav_add -> {
                createProject()
            }
            R.id.nav_project -> {
                allProjects()
            }
            R.id.nav_tasks -> {
                myTasks()
            }
        }
        return true
    }

    fun showProfile() {
        intent = Intent(this, EditProfileActivity::class.java)
        startActivity(intent)
    }

    fun logOut() {

    }

    fun returnHome() {
        intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
    }

    fun createProject() {
        intent = Intent(this, CreateProjectActivity::class.java)
        startActivity(intent)
    }

    fun myTasks() {
        intent = Intent(this, MyTasksActivity::class.java)
        startActivity(intent)
    }

    fun myFavorites() {
        intent = Intent(this, FavoritesActivity::class.java)
        startActivity(intent)
    }

    fun allProjects() {
        intent = Intent(this, AllProjectsActivity::class.java)
        startActivity(intent)
    }


}
