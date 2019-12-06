package com.mcc.g22

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.android.synthetic.main.activity_edit_profile.*
import android.content.Intent
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_dashboard.drawer_layout
import kotlinx.android.synthetic.main.activity_edit_profile.nav_view

class EditProfileActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private val user = FirebaseAuth.getInstance().currentUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        nav_view.setNavigationItemSelectedListener(this)
        bottom_nav_view.setOnNavigationItemSelectedListener(this)
/*
        edit_password_button.setOnClickListener {

            checkCurrentPassword()
        }

        update_password_button.setOnClickListener {
            updatePassword()
        }
        */
    }

    private fun updatePassword() {

        val password = new_password_editText.text.toString()
        val confirmPassword = confirm_new_password_editText.text.toString()

        if(password != confirmPassword){
            confirm_new_password_editText.error = resources.getString(R.string.password_not_match)
            return
        }
        if(user?.email != null) {
            user.updatePassword(password)
                .addOnCompleteListener{
                    if(it.isSuccessful){
                        Toast.makeText(this , resources.getString(R.string.password_updated),Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this , it.message , Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun checkCurrentPassword() {

        var currentPassword = current_password_editText.text.toString()
        if (currentPassword.isEmpty()) {
            current_password_editText.error = resources.getString(R.string.password_required)
            return
        }

        if(user?.email != null){
            val authCredential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            //TODO create a progressbar is better
            user.reauthenticate(authCredential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {

                    }
                    if (it.exception is FirebaseAuthInvalidCredentialsException) {
                        current_password_editText.error =
                            resources.getString(R.string.invalid_password)
                    }

                }
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


    fun logOut() {
        intent = Intent(this, ProjectTasksActivity::class.java)
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
