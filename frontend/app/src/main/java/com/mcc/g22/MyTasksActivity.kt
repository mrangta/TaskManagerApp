package com.mcc.g22

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import com.mcc.g22.utils.logOut
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_my_tasks.*
import kotlinx.android.synthetic.main.activity_my_tasks.bottom_nav_view

class MyTasksActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var ongoingTasks = mutableListOf<Task>()
    private var completedTasks = mutableListOf<Task>()
    private var pendingTasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tasks)

        nav_view.setNavigationItemSelectedListener(this)
        bottom_nav_view.setOnNavigationItemSelectedListener(this)

        User.getRegisteredUser()!!.getUsersTasks({
            for (t in it) {
                when (t.status) {
                    Task.TaskStatus.ON_GOING -> {
                        ongoingTasks.add(t)
                    }
                    Task.TaskStatus.PENDING -> {
                        pendingTasks.add(t)
                    }
                    else -> completedTasks.add(t)
                }
            }

            val adapterOngoing = ArrayAdapter(this, R.layout.task,
                    ongoingTasks)
            val adapterPending = ArrayAdapter(this, R.layout.task,
                    pendingTasks)
            val adapterCompleted = ArrayAdapter(this, R.layout.task,
                    completedTasks)

            runOnUiThread {
                ongoingList.adapter = adapterOngoing
                ongoingList.setOnItemClickListener { parent, view, position, id ->
                    intent = Intent(this, CreateTaskActivity::class.java)
                    CreateTaskActivity.task = ongoingTasks[position]
                    startActivity(intent)
                }
                pendingList.adapter = adapterPending
                pendingList.setOnItemClickListener { parent, view, position, id ->
                    intent = Intent(this, CreateTaskActivity::class.java)
                    CreateTaskActivity.task = pendingTasks[position]
                    startActivity(intent)
                }
                completedList.adapter = adapterCompleted
                completedList.setOnItemClickListener { parent, view, position, id ->
                    intent = Intent(this, CreateTaskActivity::class.java)
                    CreateTaskActivity.task = completedTasks[position]
                    startActivity(intent)
                }
            }
        }, {

            Toast.makeText(this, "Error while loading tasks", Toast.LENGTH_LONG)
                    .show()
        })
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
}
