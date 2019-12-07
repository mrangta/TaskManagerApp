package com.mcc.g22

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_create_project.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class CreateProjectActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val GALLERY_REQUEST_CODE = 13
    }

    private var arrayList = ArrayList<String>()
    private var membersArrayList = ArrayList<String>()

    private lateinit var adapter : ArrayAdapter<String>
    private lateinit var imageView: ImageView

    private lateinit var membersAdapter: ArrayAdapter<String>
    private lateinit var addMembers: AutoCompleteTextView

    private var projectBadge: Uri? = null

    private var usernameToUid = HashMap<String, String>()

    private val cal = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_project)
        nav_view.setNavigationItemSelectedListener(this)
        bottom_nav_view.setOnNavigationItemSelectedListener(this)

        imageView = findViewById(R.id.profile_picture)

        project_type.setOnClickListener {
            val popupMenu = PopupMenu(this, project_type)
            popupMenu.menuInflater.inflate(R.menu.project_type, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.personal ->
                        project_type.text = "Personal"
                    R.id.group ->
                        project_type.text = "Group"
                }
                true
            })
            popupMenu.show()
        }

        adapter = ArrayAdapter(this,
            R.layout.keyword, arrayList)

        membersAdapter = ArrayAdapter(this, R.layout.keyword, membersArrayList)

        keyword_list.adapter = adapter
        members_list_create_project.adapter = membersAdapter

        project_keywords.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.action == KeyEvent.ACTION_UP) {
                    arrayList.add(project_keywords.text.toString())
                    adapter.notifyDataSetChanged()
                    project_keywords.setText("")
                    return@OnKeyListener true
                }
            }
            false
        })

        findViewById<ImageButton>(R.id.imageButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/jpg")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        addMembers = findViewById(R.id.add_members_complete_text_view)
        val members = mutableListOf<String>()
        var adapter: ArrayAdapter<String> = ArrayAdapter(this,
                android.R.layout.select_dialog_item,
                members)
        adapter.setNotifyOnChange(true)
        addMembers.setAdapter(adapter)
        addMembers.threshold = 3
        addMembers.addTextChangedListener {
            User.searchForUsers(addMembers.text.toString(), {
                members.clear()
                for (m in it) {
                    members.add(m.second)
                    usernameToUid[m.second] = m.first
                }
                runOnUiThread {
                    adapter = ArrayAdapter(this,
                    android.R.layout.select_dialog_item,
                    members)
                    addMembers.setAdapter(adapter)
                    adapter.notifyDataSetChanged()
                    addMembers.showDropDown()
                }
            }, {

            })
        }
        addMembers.setOnItemClickListener { parent, view, position, id ->
            membersArrayList.add( members[position] )
            membersAdapter.notifyDataSetChanged()
        }

        findViewById<Button>(R.id.create_project).setOnClickListener {

            val title = findViewById<EditText>(R.id.project_title).text.toString()
            val description = findViewById<EditText>(R.id.project_description).text.toString()
            val keywords = arrayList
            val isPrivate = findViewById<TextView>(R.id.project_type).text.toString() == "Personal"
            val badge = projectBadge
            var deadline: Instant? = null
            if (due_date.text.toString().isNotEmpty())
                deadline = cal.toInstant()
            val membersToAdd = mutableSetOf<String>()
            for (m in membersArrayList) {
                membersToAdd.add( usernameToUid[m]!! )
            }

            val progress = ProgressDialog(this)
            progress.setMessage(getString(R.string.creating_a_project))
            progress.setCancelable(false)
            progress.show()

            Project.createProject(title, isPrivate, description,
                    keywords.toTypedArray(),
                    membersToAdd.toTypedArray(), {

                runOnUiThread {
                    progress.hide()
                    allProjects()
                }
            }, {

                runOnUiThread {
                    Toast.makeText(this,
                        "Error while creating a project", Toast.LENGTH_LONG).show()
                }
            }, deadline = deadline, badge = badge)
        }
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        pick_date!!.setOnClickListener {
            DatePickerDialog(this@CreateProjectActivity,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.UK)
        due_date!!.setText(sdf.format(cal.getTime()))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return
        if (resultCode == Activity.RESULT_OK) when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                //data.getData returns the content URI for the selected Image
                projectBadge = data.data
                imageView.setImageURI(projectBadge)
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
