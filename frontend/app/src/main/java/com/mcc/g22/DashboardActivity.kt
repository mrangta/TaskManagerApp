package com.mcc.g22


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat.*
import kotlinx.android.synthetic.main.activity_dashboard.*
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import androidx.recyclerview.widget.RecyclerView


class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var pRecyclerView: RecyclerView? = null
    private var pAdapter: RecyclerView.Adapter<*>? = null
    var listOfprojects: ArrayList<ProjectListDetails> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        nav_view.setNavigationItemSelectedListener(this)

        //adding items in list
        for (i in 0..1) {
            val project = ProjectListDetails()
            project.id = i
            project.project_title = "Project Title $i"
            listOfprojects!!.add(project)
        }
        pRecyclerView = findViewById(R.id.projectRecyclerView)
        var pLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        pRecyclerView!!.layoutManager = pLayoutManager
        pAdapter = ProjectListAdapter(listOfprojects)
        pRecyclerView!!.adapter = pAdapter


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

   


}
