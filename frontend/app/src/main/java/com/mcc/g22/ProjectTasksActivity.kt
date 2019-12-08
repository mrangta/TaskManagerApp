package com.mcc.g22

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.mcc.g22.utils.logOut
import kotlinx.android.synthetic.main.activity_my_tasks.bottom_nav_view
import kotlinx.android.synthetic.main.activity_my_tasks.completedList
import kotlinx.android.synthetic.main.activity_my_tasks.drawer_layout
import kotlinx.android.synthetic.main.activity_my_tasks.nav_view
import kotlinx.android.synthetic.main.activity_my_tasks.ongoingList
import kotlinx.android.synthetic.main.activity_my_tasks.pendingList
import kotlinx.android.synthetic.main.activity_project_tasks.*

class ProjectTasksActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var ongoingTasks = mutableListOf<Task>()
    private var completedTasks = mutableListOf<Task>()
    private var pendingTasks = mutableListOf<Task>()

    companion object {
        var project: Project? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_tasks)

        if (project == null) {
            finish()
            return
        }

        showUserInfoInMenu()

        val p = project as Project

        nav_view.setNavigationItemSelectedListener(this)
        bottom_nav_view.setOnNavigationItemSelectedListener(this)

        desc_content.text = p.description
        p.loadBadgeIntoImageView(this, profile_picture)
        modified_date.text = p.lastModificationDate.toString()

        val adapterOngoing = ArrayAdapter(this, R.layout.task,
                ongoingTasks)
        val adapterPending = ArrayAdapter(this, R.layout.task,
                pendingTasks)
        val adapterCompleted = ArrayAdapter(this, R.layout.task,
                completedTasks)

        ongoingList.adapter = adapterOngoing
        pendingList.adapter = adapterPending
        completedList.adapter = adapterCompleted

        for (t in p.tasksIds) {
            Task.getTaskFromDatabase(t, {
                when (it.status) {
                    Task.TaskStatus.ON_GOING -> {
                        ongoingTasks.add(it)
                        runOnUiThread { adapterOngoing.notifyDataSetChanged() }
                    }
                    Task.TaskStatus.PENDING -> {
                        pendingTasks.add(it)
                        runOnUiThread { adapterPending.notifyDataSetChanged() }
                    }
                    else -> {
                        completedTasks.add(it)
                        runOnUiThread { adapterCompleted.notifyDataSetChanged() }
                    }
                }
            }, {
                Toast.makeText(this, "Error while loading task", Toast.LENGTH_LONG)
                        .show()
            })
        }
    }


    private fun showUserInfoInMenu(){

        var user = User.getRegisteredUser()
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.username_menu_textView).text = user!!.username
        user!!.showProfileImage(this , nav_view.getHeaderView(0).findViewById(R.id.profile_picture_menu_imageView))

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
