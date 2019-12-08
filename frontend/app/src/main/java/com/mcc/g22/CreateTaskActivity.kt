package com.mcc.g22

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.mcc.g22.User.Companion.resolveDisplayName
import com.mcc.g22.utils.logOut
import kotlinx.android.synthetic.main.activity_create_task.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class CreateTaskActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemSelectedListener {

    private lateinit var addMembers: AutoCompleteTextView
    private var usernameToUid = HashMap<String, String>()

    private var membersArrayList = ArrayList<String>()
    private lateinit var membersAdapter: ArrayAdapter<String>

    private val cal = Calendar.getInstance()

    companion object {
        var task: Task? = null
        private const val GALLERY_REQUEST_CODE = 14
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        nav_view.setNavigationItemSelectedListener(this)
        bottom_nav_view.setOnNavigationItemSelectedListener(this)

        showUserInfoInMenu()

        task_status.setOnClickListener {
            val popupMenu: PopupMenu = PopupMenu(this, task_status)
            popupMenu.menuInflater.inflate(R.menu.task_status, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.ongoing ->
                        task_status.text = "Ongoing"
                    R.id.pending ->
                        task_status.text = "Pending"
                    R.id.completed ->
                        task_status.text = "Completed"
                }
                true
            })
            popupMenu.show()
        }

        addMembers = findViewById(R.id.assigned_to)

        if (task == null) {
            task_status.visibility = View.INVISIBLE
            task_status_label.visibility = View.INVISIBLE
            membersAdapter = ArrayAdapter(this, R.layout.keyword, membersArrayList)
            members_list_create_task.adapter = membersAdapter
        } else {
            create_new_task.text = "Edit Existing Task"
            add_task.text = "Edit Task"
            task_description.setText(task!!.description)
            membersArrayList = arrayListOf()

            membersAdapter = ArrayAdapter(this, R.layout.keyword, membersArrayList)
            members_list_create_task.adapter = membersAdapter

            for (user in task!!.getAssignedUsers()) {
                resolveDisplayName(user, {
                    membersArrayList.add(it)
                    membersAdapter.notifyDataSetChanged()
                }, {
                    runOnUiThread { Toast.makeText(this, "Error in retrieving assigned users", Toast.LENGTH_LONG).show() }
                })
            }

            val zdtTask: ZonedDateTime = ZonedDateTime.ofInstant(task!!.deadline, ZoneOffset.UTC)
            val calendar = GregorianCalendar.from(zdtTask)
            cal.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
            updateDateInView()

            task_status.text = when (task!!.status) {
                Task.TaskStatus.PENDING -> "Pending"
                Task.TaskStatus.ON_GOING -> "Ongoing"
                Task.TaskStatus.COMPLETED -> "Completed"
            }
            create_task.text = "Edit Task"
        }

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
            task?.changeDeadline(cal.toInstant())
        }

        pick_date!!.setOnClickListener {
            DatePickerDialog(this@CreateTaskActivity,
                    dateSetListener,
                    // set DatePickerDialog to point to today's date when it loads up
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
        }

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
                    if (ProjectTasksActivity.project!!.membersIds.contains(m.first)) {
                        members.add(m.second)
                        usernameToUid[m.second] = m.first
                    }
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
            addMembers.setText("")
        }

        if (task == null) {

            create_task.setOnClickListener {
                createTaskListener()
            }

            findViewById<ImageButton>(R.id.imageButton).setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                val mimeTypes = arrayOf("image/jpeg", "image/jpg")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

                startActivityForResult(intent, GALLERY_REQUEST_CODE)
            }
        } else {
            create_task.setOnClickListener {
                editTaskListener()
            }

            findViewById<ImageButton>(R.id.imageButton).visibility = View.INVISIBLE
        }

        if (task != null) {
            if (ProjectTasksActivity.project == null) {
                Project.fromProjectId(task!!.projectId, {
                    runOnUiThread {
                        if (it.isPrivate ||
                            !(it.isUserAdmin(User.getRegisteredUser()!!))
                        ) {
                            addMembers.visibility = View.GONE
                            members_list_create_task.visibility = View.GONE
                            assigned_to_label.visibility = View.GONE
                        }
                    }
                }, {
                    runOnUiThread { Toast.makeText(this, "Error: cannot fetch project information",
                        Toast.LENGTH_LONG).show() }
                })
            } else {
                if (ProjectTasksActivity.project!!.isPrivate ||
                    !(ProjectTasksActivity.project!!.isUserAdmin(User.getRegisteredUser()!!))
                ) {
                    addMembers.visibility = View.GONE
                    members_list_create_task.visibility = View.GONE
                    assigned_to_label.visibility = View.GONE
                }
            }
        }
    }

    private fun createTaskListener() {
        val desc = findViewById<EditText>(R.id.task_description).text.toString()
        val deadline: Instant?
        if (due_date.text.toString().isNotEmpty())
            deadline = cal.toInstant()
        else {
            Toast.makeText(this, "Deadline must be defined", Toast.LENGTH_LONG).show()
            return
        }
        val membersToAdd = mutableSetOf<String>()
        for (m in membersArrayList) {
            membersToAdd.add( usernameToUid[m]!! )
        }

        val progress = ProgressDialog(this)
        progress.setMessage(getString(R.string.creating_a_task))
        progress.setCancelable(false)
        progress.show()

        val t = Task.createTask(ProjectTasksActivity.project!!.projectId,
            desc, deadline, membersToAdd)

        if (task_status.visibility != View.INVISIBLE) {
            when (task_status.text) {
                "Ongoing" -> {
                    t.makeTaskOnGoing()
                }
                "Pending" -> {
                    t.makeTaskPending()
                }
                "Completed" -> {
                    t.makeTaskCompleted()
                }
            }
        }

        t.commitChanges({

            runOnUiThread {
                progress.dismiss()
                finish()
            }
        }, {
            runOnUiThread {
                Toast.makeText(this, "Error while creating a task", Toast.LENGTH_LONG).show()
                progress.dismiss()
            }})
    }

    private fun editTaskListener() {

        when (task_status.text) {
            "Ongoing" -> {
                if (task?.status != Task.TaskStatus.ON_GOING) task?.makeTaskOnGoing()
            }
            "Pending" -> {
                if (task?.status != Task.TaskStatus.PENDING) task?.makeTaskPending()
            }
            "Completed" -> {
                if (task?.status != Task.TaskStatus.COMPLETED) task?.makeTaskCompleted()
            }
        }

        if (task?.description != task_description.text.toString()) {
            task?.changeTaskDescription(task_description.text.toString())
        }

        val progress = ProgressDialog(this)
        progress.setMessage(getString(R.string.editing_a_task))
        progress.setCancelable(false)
        progress.show()

        val newUsers = arrayListOf<String>()
        val assignedUsers = task!!.getAssignedUsers()
        for (m in membersArrayList) {
            val uid = usernameToUid[m]
            if ((uid != null) && !(assignedUsers.contains(uid))) {
                newUsers.add(uid)
            }
        }
        task?.assignUsers(newUsers.toTypedArray())

        task?.commitChanges({

            runOnUiThread {
                progress.hide()
                finish()
            }
        }, {
            runOnUiThread {
                Toast.makeText(this, "Error while creating a task", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) return
        if (resultCode == Activity.RESULT_OK) when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                try {
                    Task.createTask(this, ProjectTasksActivity.project!!.projectId,
                            data.data!!, {

                        runOnUiThread { task_description.setText(it.description) }
                    }, {
                        Toast.makeText(this, "Error while creating a task", Toast.LENGTH_LONG).show()
                    })
                } catch (e: Exception) {

                }
            }
        }
    }

    private fun updateDateInView() {
        val myFormat = "dd/MM/yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.UK)
        due_date!!.setText(sdf.format(cal.getTime()))
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

    override fun onDestroy() {
        super.onDestroy()
        task = null
    }
}
