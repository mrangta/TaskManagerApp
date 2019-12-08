package com.mcc.g22

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_edit_profile.*
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import com.mcc.g22.utils.logOut
import androidx.core.view.GravityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.android.synthetic.main.activity_dashboard.drawer_layout
import kotlinx.android.synthetic.main.activity_edit_profile.nav_view


class EditProfileActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var user = User.getRegisteredUser()
    private var profilePhoto: Uri? = null
    private val authUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        nav_view.setNavigationItemSelectedListener(this)
        bottom_nav_view.setOnNavigationItemSelectedListener(this)

        getUserInfo()

        imageButton_profileSetting.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
            user!!.showProfileImage(this , nav_view.getHeaderView(0).findViewById(R.id.profile_picture_menu_imageView))

        }

        confirm_new_password_editText.setOnFocusChangeListener { _, hasFocus ->
            var newPassword = new_password_editText.text.toString()
            var confirmPassword = confirm_new_password_editText.text.toString()

            if (hasFocus) {
                password_not_matched.text = ""
                password_not_matched.visibility = View.GONE
            }
            if (!hasFocus) {
                if(newPassword != confirmPassword){
                    password_not_matched.text = resources.getString(R.string.password_not_match)
                    password_not_matched.visibility = View.VISIBLE
                }
            }
        }

        update_password_button.setOnClickListener {
            checkCurrentPassword()
        }
    }

    fun onRadioButtonClicked(view: View) {
        var checked = view as RadioButton
        if (low_res == checked) {

        }
        if (high_res == checked) {

        }
        if (full_res == checked) {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){

            profilePhoto = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, profilePhoto)
            profile_picture_profileSetting.setImageBitmap(bitmap)
            user!!.setProfileImage(profilePhoto!! , {}, {})
          //  user!!.showProfileImage(this , nav_view.getHeaderView(0).findViewById(R.id.profile_picture_menu_imageView))

        }
    }

    private fun showUserInfoInMenu(){

        var user = User.getRegisteredUser()
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.username_menu_textView).text = user!!.username
        user!!.showProfileImage(this , nav_view.getHeaderView(0).findViewById(R.id.profile_picture_menu_imageView))
    }

    private fun getUserInfo(){

        showUserInfoInMenu()
        username_profileSetting_textView.text = user!!.username
        email_profileSetting_textView.text = user!!.email
        user!!.showProfileImage(this, profile_picture_profileSetting)
    }
    private fun updatePassword() {

        val password = new_password_editText.text.toString()
        val confirmPassword = confirm_new_password_editText.text.toString()

        if(password != confirmPassword){
            return
        }
        if(authUser?.email != null) {
            authUser.updatePassword(password)
                .addOnCompleteListener{

                    progressbar_update_password.visibility = View.GONE
                    password_not_matched.text = ""
                    password_not_matched.visibility = View.GONE

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

        if(authUser != null){
            val authCredential = EmailAuthProvider.getCredential(authUser!!.email!!, currentPassword)

            progressbar_update_password.visibility = View.VISIBLE
            authUser.reauthenticate(authCredential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        updatePassword()
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
            user!!.showProfileImage(this , nav_view.getHeaderView(0).findViewById(R.id.profile_picture_menu_imageView))
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
