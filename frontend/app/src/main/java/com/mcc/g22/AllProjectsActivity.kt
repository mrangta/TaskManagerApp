package com.mcc.g22

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_dashboard.*
import com.mcc.g22.utils.logOut
import kotlinx.android.synthetic.main.activity_all_projects.*
import kotlinx.android.synthetic.main.activity_dashboard.bottom_nav_view
import kotlinx.android.synthetic.main.activity_dashboard.drawer_layout
import kotlinx.android.synthetic.main.activity_dashboard.nav_view
import java.util.ArrayList

class AllProjectsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var pRecyclerView: RecyclerView? = null
    private var pAdapter: RecyclerView.Adapter<*>? = null

    private lateinit var allProjects: Set<Project>
    private var currentlyDisplayedProjects = ArrayList<Project>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_projects)
        nav_view.setNavigationItemSelectedListener(this)
        bottom_nav_view.setOnNavigationItemSelectedListener(this)

        showUserInfoInMenu()
        User.getRegisteredUser()!!.getUsersProjects({
            allProjects = it
            currentlyDisplayedProjects = it.toMutableList() as ArrayList<Project>
            runOnUiThread {
                //adding projects in list
                pRecyclerView = findViewById(R.id.projectRecyclerView)
                val pLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                pRecyclerView!!.layoutManager = pLayoutManager
                pAdapter = ProjectListAdapter(currentlyDisplayedProjects)
                { itemDto: Project, position: Int ->
                    intent = Intent(this, ProjectTasksActivity::class.java)
                    ProjectTasksActivity.project = itemDto
                    startActivity(intent)
                }
                pRecyclerView!!.adapter = pAdapter
            }
        }, {

            Toast.makeText(this, "Error while fetching projects", Toast.LENGTH_LONG).show()
        })

        search_project.addTextChangedListener {
            val currentText = search_project.text.toString()

            if (currentText.isEmpty()) {
                currentlyDisplayedProjects = allProjects.toMutableList() as ArrayList<Project>
                pAdapter?.notifyDataSetChanged()
                return@addTextChangedListener
            }

            val newProjects = ArrayList<Project>()
            for (p in allProjects) {

                var isFoundInKeyword = false
                for (keyword in p.keywords) {
                    if (keyword.contains(currentText)) {
                        isFoundInKeyword = true
                        break
                    }
                }
                if (p.name.contains(currentText) ||
                        isFoundInKeyword) {

                    newProjects.add(p)
                }
            }
            currentlyDisplayedProjects = newProjects
            pAdapter = ProjectListAdapter(currentlyDisplayedProjects as ArrayList<Project>)
            { itemDto: Project, position: Int ->
                intent = Intent(this, ProjectTasksActivity::class.java)
                ProjectTasksActivity.project = itemDto
                startActivity(intent)
            }
            pRecyclerView!!.swapAdapter(pAdapter, false)
        }
    }

    fun toggleDrawer(view: View){
        if(drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        }
        else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed(){
        if(drawer_layout.isDrawerOpen(GravityCompat.START))
            drawer_layout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
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

    private fun showUserInfoInMenu(){

        val user = User.getRegisteredUser()
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.username_menu_textView).text = user!!.username
        user.showProfileImage(this , nav_view.getHeaderView(0).findViewById(R.id.profile_picture_menu_imageView))

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
