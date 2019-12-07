package com.mcc.g22

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.GravityCompat.*
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mcc.g22.utils.logout
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_dashboard.bottom_nav_view
import kotlinx.android.synthetic.main.activity_dashboard.drawer_layout
import kotlinx.android.synthetic.main.activity_dashboard.nav_view
import kotlinx.android.synthetic.main.nav_header.*

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var uid = FirebaseAuth.getInstance().currentUser!!.uid
    private val database = FirebaseDatabase.getInstance()
    private lateinit var currentUser :User

    private var pRecyclerView: RecyclerView? = null
    private var pAdapter: RecyclerView.Adapter<*>? = null
    var listOfprojects: ArrayList<ProjectListDetails> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        nav_view.setNavigationItemSelectedListener(this)
        bottom_nav_view.setOnNavigationItemSelectedListener(this)

        getUserInfo({
            currentUser = it

            val name = currentUser.username
           // Log.d("" , "USERNAME IN UP IS $name")
            welcome.text = (resources.getString(R.string.welcome) + "  " + name)

            Log.d("" , "USERNAME IN UP IS $name")
            nav_view.getHeaderView(0).findViewById<TextView>(R.id.username_menu_textView).text = name

            currentUser.showProfileImage(this , profile_picture_dashboard)
            currentUser.showProfileImage(this , nav_view.getHeaderView(0).findViewById(R.id.profile_picture_menu_imageView))
         },{ })


        for (i in 0..1) {
            val project = ProjectListDetails()
            project.id = i
            project.project_title = "Project Title $i"
            listOfprojects!!.add(project)
        }
        pRecyclerView = findViewById(R.id.projectRecyclerView)
        var pLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        pRecyclerView!!.layoutManager = pLayoutManager
        pAdapter = ProjectListAdapter(listOfprojects){ itemDto: ProjectListDetails, position: Int ->
            intent = Intent(this, ProjectTasksActivity::class.java)
            intent.putExtra("project_title", listOfprojects[position].project_title)
            startActivity(intent)
        }
        pRecyclerView!!.adapter = pAdapter


    }

    private fun getUserInfo(onLogedIn: (user : User) -> Unit , onLogedOut:() -> Unit) {

         database.getReference("users").child(uid).addListenerForSingleValueEvent(object:
             ValueEventListener {
              override fun onDataChange(dataSnapshot: DataSnapshot) {
                  if(dataSnapshot.exists()){
                      val user = dataSnapshot.getValue(User::class.java)!!
                      currentUser = user
                      onLogedIn(user)
                    }
                  else onLogedOut()
              }
              override fun onCancelled(error: DatabaseError) {
                  onLogedOut()
              }
          })
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

        val alert = AlertDialog.Builder(this)
        alert.setTitle("Confirm")
        alert.setMessage(resources.getString(R.string.alertExit))

        alert.setPositiveButton("YES") { dialog, yes ->
            FirebaseAuth.getInstance().signOut()
            logout()
        }
        alert.setNegativeButton("No") { dialog, no ->
        }

        val dialog: AlertDialog = alert.create()
        dialog.show()
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
