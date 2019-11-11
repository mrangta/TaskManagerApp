package com.mcc.g22

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.database.*

class NotificationsService : Service() {
    private lateinit var assignChangedListener: ChildEventListener
    private lateinit var projectsChangedListener: ChildEventListener
    private lateinit var binder: IBinder
    private lateinit var database: DatabaseReference

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val usernameToWatch = intent!!.extras!!.get(USERNAME_TO_WATCH) as String
        database = FirebaseDatabase.getInstance().reference

        // Listen for adding to a new project
        projectsChangedListener = object : ChildEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "projectsChanged:onCancelled", databaseError.toException())
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, p1: String?) {
                // Left empty intentionally
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
                // Left empty intentionally
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {

                // Value must be true
                if (!(dataSnapshot.value as Boolean)) return
                showAddedToProjectNotification(usernameToWatch, dataSnapshot.key as String)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // Left empty intentionally
            }
        }
        database.child("users")
                .child(usernameToWatch)
                .child("projects")
                .addChildEventListener(projectsChangedListener)

        // Listen to be assign to a new task
        assignChangedListener = object : ChildEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "assignChanged:onCancelled", databaseError.toException())
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, p1: String?) {
                // Left empty intentionally
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
                // Left empty intentionally
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {

                // Value must be true
                if (!(dataSnapshot.value as Boolean)) return
                showAssignedToTaskNotification(usernameToWatch, dataSnapshot.key as String)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // Left empty intentionally
            }
        }
        database.child("users")
            .child(usernameToWatch)
            .child("tasks")
            .addChildEventListener(assignChangedListener)

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

    private fun showAddedToProjectNotification(username: String, projectId: String) {

    }

    private fun showAssignedToTaskNotification(username: String, taskId: String) {

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
