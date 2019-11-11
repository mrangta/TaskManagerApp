package com.mcc.g22

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.database.*

class NotificationsService : Service() {
    private lateinit var assignChangedListener: ValueEventListener
    private lateinit var projectsChangedListener: ValueEventListener
    private lateinit var binder: IBinder
    private lateinit var database: DatabaseReference

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val usernameToWatch = intent!!.extras!!.get(USERNAME_TO_WATCH) as String
        database = FirebaseDatabase.getInstance().reference

        // Listen for adding to a new project
        projectsChangedListener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "projectsChanged:onCancelled", databaseError.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val projectName = "no project name"
                showAddedToProjectNotification(usernameToWatch, projectName)
            }
        }
        database.child("users")
                .child(usernameToWatch)
                .child("projects")
                .addValueEventListener(projectsChangedListener)

        // Listen to be assign to a new task
        assignChangedListener = object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "assignChanged:onCancelled", databaseError.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val taskTitle = "no task title"
                showAssignedToTaskNotification(usernameToWatch, taskTitle)
            }
        }
        database.child("users")
            .child(usernameToWatch)
            .child("tasks")
            .addValueEventListener(assignChangedListener)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        database.removeEventListener(projectsChangedListener)
        database.removeEventListener(assignChangedListener)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun showAddedToProjectNotification(username: String, projectName: String) {

    }

    private fun showAssignedToTaskNotification(username: String, taskTitle: String) {

    }

    private fun showApproachingDeadlineNotification(username: String) {

    }

    companion object {
        private const val USERNAME_TO_WATCH: String = "USERNAME_TO_WATCH"
        private const val TAG: String = "MCC"

        fun startNotificationService(ctx: Context, username: String) {
            val i = Intent(ctx, NotificationsService::class.java)
            i.putExtra(USERNAME_TO_WATCH, username)
            ctx.startService(i)
        }
    }
}
