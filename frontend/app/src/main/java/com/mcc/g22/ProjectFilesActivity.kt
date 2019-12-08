package com.mcc.g22

import android.app.Activity
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_my_tasks.bottom_nav_view
import kotlinx.android.synthetic.main.activity_my_tasks.drawer_layout
import kotlinx.android.synthetic.main.activity_my_tasks.nav_view
import kotlinx.android.synthetic.main.activity_project_files.*
import kotlinx.android.synthetic.main.activity_project_picture.project_title_layout


class ProjectFilesActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private var customAdapter: CustomAdapter? = null
    private var imageModelArrayList: ArrayList<ImageModel>? = null

    private var imageList = mutableListOf<Int>() //R.drawable.ic_tasks
    private var myImageNameList = mutableListOf<String>()

    companion object {
        private const val FILE_SELECT_CODE = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_files)

        nav_view.setNavigationItemSelectedListener(this)
        bottom_nav_view.setOnNavigationItemSelectedListener(this)

        val p = ProjectTasksActivity.project as Project

        project_title_layout.text = p.name

        p.attachmentsManager.listAllAttachments({ attachments ->

            for (d in attachments) {

                if (d.substringAfterLast('.') != "jpg") {
                    myImageNameList.add(d)
                    imageList.add(R.drawable.ic_tasks)
                }
            }

            runOnUiThread {
                if (myImageNameList.size > 0) {
                    imageModelArrayList = populateList()
                    customAdapter = CustomAdapter(this, imageModelArrayList!!)
                    files_list!!.adapter = customAdapter
                    addListListener()
                }
            }

        }, {
            runOnUiThread { Toast.makeText(this, "Error while fetching documents", Toast.LENGTH_LONG).show() }
        })

        upload_pic_button.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)

            try {
                startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE
                )
            } catch (ex: ActivityNotFoundException) { // Potentially direct the user to the Market with a Dialog
                Toast.makeText(
                    this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return
        when (requestCode) {
            FILE_SELECT_CODE -> if (resultCode == Activity.RESULT_OK) { // Get the Uri of the selected file
                val uri: Uri? = data.data
                if (uri != null) {

                    val progress = ProgressDialog(this)
                    progress.setMessage(getString(R.string.uploading_file))
                    progress.setCancelable(false)
                    progress.show()

                    ProjectTasksActivity.project!!.attachmentsManager.uploadFile(this, uri, {
                        runOnUiThread {
                            progress.hide()
                            Toast.makeText(this, "File uploaded", Toast.LENGTH_LONG).show()

                            myImageNameList.add(uri.lastPathSegment!!.substringAfterLast('/'))
                            imageList.add(R.drawable.ic_tasks)

                            imageModelArrayList = populateList()
                            customAdapter = CustomAdapter(this, imageModelArrayList!!)
                            files_list!!.adapter = customAdapter
                        }
                    }, {
                        runOnUiThread {
                            progress.hide()
                            Toast.makeText(this, "Error occurred", Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
        }
    }

    private fun populateList(): ArrayList<ImageModel> {

        val list = ArrayList<ImageModel>()

        var i = 0
        while (i < imageList.size) {
            val imageModel = ImageModel()
            imageModel.setNames(myImageNameList[i])
            imageModel.setImage_drawables(imageList[i])
            list.add(imageModel)
            i++
        }

        return list
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

    private fun addListListener() {
        files_list!!.setOnItemClickListener { parent, view, position, id ->
            ProjectTasksActivity.project!!.attachmentsManager.downloadFile(myImageNameList[position], {

                val toLaunch = Intent()
                toLaunch.action = Intent.ACTION_VIEW
                toLaunch.setDataAndType(
                    FileProvider.getUriForFile(this,
                        this.packageName + ".fileprovider", it),
                    "application/" + it.extension)
                toLaunch.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                val contentIntent =
                    PendingIntent.getActivity(this, 0, toLaunch, 0)

                val notificationHelper =
                    NotificationHelper(this, "Download",
                        "Download", 3)

                notificationHelper.showNotification(
                    "Downloading completed!",
                    it.absolutePath + " ready to open",
                    onTapAction = contentIntent)
            }, {
                runOnUiThread { Toast.makeText(this, "Error while downloading the file", Toast.LENGTH_LONG).show() }
            })
        }
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
