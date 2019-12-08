package com.mcc.g22

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.view.GravityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_my_tasks.*
import kotlinx.android.synthetic.main.activity_my_tasks.bottom_nav_view
import kotlinx.android.synthetic.main.activity_my_tasks.completedList
import kotlinx.android.synthetic.main.activity_my_tasks.drawer_layout
import kotlinx.android.synthetic.main.activity_my_tasks.nav_view
import com.mcc.g22.utils.logOut
import kotlinx.android.synthetic.main.activity_project_tasks.*

class SingleTaskActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemSelectedListener {

    var array = arrayOf("Create backend for the project", "Write documentation", "Write documentation", "Write documentation", "Write documentation", "Write documentation", "Write documentation", "Write documentation", "Write documentation", "Write documentation", "Write documentation")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_task)

        nav_view.setNavigationItemSelectedListener(this)
        bottom_nav_view.setOnNavigationItemSelectedListener(this)

        val bundle: Bundle? = intent.extras
        val string: String? = bundle?.getString("project_title")

        project_title_layout.text = string


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

    fun createTask(view: View) {
        intent = Intent(this, CreateTaskActivity::class.java)
        startActivity(intent)
    }

    fun tasksTab(view: View) {
        intent = Intent(this, ProjectTasksActivity::class.java)
        intent.putExtra("project_title", project_title_layout.text.toString())
        startActivity(intent)
    }

    fun picturesTab(view: View) {
        intent = Intent(this, ProjectPictureActivity::class.java)
        intent.putExtra("project_title", project_title_layout.text.toString())
        startActivity(intent)
    }

    fun filesTab(view: View) {
        intent = Intent(this, ProjectFilesActivity::class.java)
        intent.putExtra("project_title", project_title_layout.text.toString())
        startActivity(intent)
    }
}
