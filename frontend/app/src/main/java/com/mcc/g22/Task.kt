package com.mcc.g22

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.mcc.g22.apiclient.ApiClient
import com.mcc.g22.apiclient.models.InlineObject1
import com.mcc.g22.apiclient.models.InlineObject2
import java.io.File
import java.time.Instant
import kotlin.concurrent.thread
import kotlin.math.abs
import kotlin.random.Random

/**
 * Task to be added to a project
 */
class Task {

    enum class TaskStatus(val stringValue: String) {
        PENDING("pending"),
        ON_GOING("on-going"),
        COMPLETED("completed")
    }

    var projectId: String = ""
        private set

    private var alarmRequestCode: Int = -1

    var name: String = ""

    var description: String = ""
        private set

    var deadline: Instant = Instant.now()
        private set

    var status: TaskStatus = TaskStatus.PENDING
        private set

    private var assignedUsersIds: MutableSet<String> = mutableSetOf()
    private var taskId: String = ""

    private var statusHasChanged: Boolean = false
    private var taskHasBeenCreated: Boolean = false
    private var usersHaveBeenAssigned: Boolean = false
    private var descriptionHasChanged: Boolean = false

    companion object {

        /**
         * Create new pending task with empty description and deadline set to now
         */
        fun createTask(projectId: String): Task {
            val t = Task()
            t.projectId = projectId
            t.taskHasBeenCreated = true
            return t
        }

        /**
         * Read task from the database
         */
        fun getTaskFromDatabase(taskId: String, onTaskReady: (task: Task) -> Unit,
                       onFailure: () -> Unit) {

            val taskInDatabase = FirebaseDatabase.getInstance().reference.child("tasks")
                                                                .child(taskId)
            taskInDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val projectId = dataSnapshot.child("projectId").value as String
                    val d = dataSnapshot.child("description").value as String
                    val s = when (dataSnapshot.child("status").value) {
                        com.mcc.g22.apiclient.models.Status.pending.value -> TaskStatus.PENDING
                        com.mcc.g22.apiclient.models.Status.ongoing.value -> TaskStatus.ON_GOING
                        com.mcc.g22.apiclient.models.Status.completed.value -> TaskStatus.COMPLETED
                        else -> {
                            onFailure()
                            return
                        }
                    }
                    val deadline = dataSnapshot.child("deadline").value as String

                    val task = createTask(projectId, d, Instant.parse(deadline)!!)

                    for (u in dataSnapshot.child("users").children) {
                        task.assignedUsersIds.add( u.key.toString() )
                    }
                    task.status = s
                    task.taskId = taskId
                    task.taskHasBeenCreated = false // task already exists
                    onTaskReady(task)
                }
            })
        }

        /**
         * Create pending task.
         */
        fun createTask(projectId: String, description: String, deadline: Instant): Task {
            val t = Task()
            t.projectId = projectId
            t.description = description
            t.deadline = deadline
            t.status = TaskStatus.PENDING
            t.taskHasBeenCreated = true
            return t
        }

        /**
         * Create task with assigned users. If number of the users is > 0, task in on-going
         */
        fun createTask(projectId: String, description: String, deadline: Instant, assignedUsers: MutableSet<String>): Task {
            val t = Task()
            t.projectId = projectId
            t.description = description
            t.deadline = deadline
            t.assignedUsersIds = assignedUsers
            if (t.assignedUsersIds.isNotEmpty()) {
                t.status = TaskStatus.ON_GOING
            }
            t.taskHasBeenCreated = true
            return t
        }

        /**
         * Create task from the API model of task object
         */
        fun createTask(projectId: String, task: com.mcc.g22.apiclient.models.Task): Task {
            val t = Task()
            t.projectId = projectId
            t.description = task.description
            t.status = when(task.status) {
                com.mcc.g22.apiclient.models.Status.pending -> TaskStatus.PENDING
                com.mcc.g22.apiclient.models.Status.ongoing -> TaskStatus.ON_GOING
                com.mcc.g22.apiclient.models.Status.completed -> TaskStatus.COMPLETED
            }
            t.deadline = Instant.parse(task.deadline)
            t.taskHasBeenCreated = true
            return t
        }

        /**
         * Convert image to pending tasks.
         * Description of the task is the text from the image.
         *
         * @param projectId ID of project in which the task is created
         * @param bitmap image to convert to task
         * @param onTaskReady function to be called when task in read to use
         * @param onFailure function to be called when text cannot be recognized
         */
        fun createTask(projectId: String, bitmap: Bitmap,
                       onTaskReady: (task: Task) -> Unit, onFailure: () -> Unit) {

            val image = FirebaseVisionImage.fromBitmap(bitmap)
            runTextDetector(image, {
                onTaskReady( createTask(projectId, it, Instant.now()) )
            }, onFailure)
        }

        /**
         * Convert image to pending tasks.
         * Description of the task is the text from the image.
         *
         * @param context application context
         * @param projectId ID of project in which the task is created
         * @param imageUri image to convert to task
         * @param onTaskReady function to be called when task in read to use
         * @param onFailure function to be called when text cannot be recognized
         */
        fun createTask(context: Context, projectId: String, imageUri: Uri,
                       onTaskReady: (task: Task) -> Unit, onFailure: () -> Unit) {

            val image = FirebaseVisionImage.fromFilePath(context, imageUri)
            runTextDetector(image, {
                onTaskReady(createTask(projectId, it, Instant.now()))
            }, onFailure)
        }

        /**
         * Convert image to pending tasks.
         * Description of the task is the text from the image.
         *
         * @param context application context
         * @param projectId ID of project in which the task is created
         * @param imageFile image to convert to task
         * @param onTaskReady function to be called when task in read to use
         * @param onFailure function to be called when text cannot be recognized
         */
        fun createTask(context: Context, projectId: String, imageFile: File,
                       onTaskReady: (task: Task) -> Unit, onFailure: () -> Unit) {
            createTask(context, projectId, Uri.fromFile(imageFile), onTaskReady, onFailure)
        }

        private fun runTextDetector(image: FirebaseVisionImage,
                                    onTextReady: (text: String) -> Unit, onFailure: () -> Unit) {

            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
            detector.processImage(image)
                .addOnSuccessListener { firebaseVisionText ->
                    onTextReady(firebaseVisionText.text)
                }
                .addOnFailureListener {
                    onFailure()
                }
        }
    }

    /**
     * Assign new user to the task
     */
    fun assignUser(u: User) {
        assignedUsersIds.add(u.uid)
        if (assignedUsersIds.size == 1 && status == TaskStatus.PENDING) {
            status = TaskStatus.ON_GOING
            statusHasChanged = true
        }
        usersHaveBeenAssigned = true
    }

    /**
     * Assigned many users to the task
     */
    fun assignUsers(users: Array<String>) {
        assignedUsersIds.addAll(users)
        if (assignedUsersIds.size >= 1 && status == TaskStatus.PENDING) {
            status = TaskStatus.ON_GOING
            statusHasChanged = true
        }
        usersHaveBeenAssigned = true
    }

    /**
     * Remove user from the task
     */
    fun removeUser(u: User) {
        assignedUsersIds.remove(u.uid)
        if (assignedUsersIds.isEmpty() && status == TaskStatus.ON_GOING) {
            status = TaskStatus.PENDING
        }
    }

    /**
     * Return users assigned to this task.
     */
    fun getAssignedUsers(): MutableSet<String> {
        return assignedUsersIds
    }

    /** Set reminder of deadline for the task.
     * @param context application context
     * @param triggerIn time in which reminder should be shown (in milliseconds)
     */
    fun setReminder(context: Context, triggerIn: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent = buildAlarmPendingIntent(context)
        alarmManager.setExact(AlarmManager.RTC,
            System.currentTimeMillis() + triggerIn,
            pendingIntent)
    }

    /**
     * Set reminder of deadline for the task.
     * @param context application context
     * @param triggerBefore how many milliseconds before task's deadline reminder should be shown
     */
    fun setReminder(context: Context, triggerBefore: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pendingIntent = buildAlarmPendingIntent(context)
        alarmManager.setExact(AlarmManager.RTC,
             deadline.toEpochMilli() - triggerBefore,
            pendingIntent)
    }

    /**
     * Cancel reminder of deadline for the task
     * @param context application context
     */
    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel( buildAlarmPendingIntent(context) )
    }

    private fun buildAlarmPendingIntent(context: Context): PendingIntent {
        var username = User.getRegisteredUser()?.username
        if (username == null) username = "(unlogged user)"

        val i = Intent(context, DeadlineAlarmReceiver::class.java)
        i.putExtra(DeadlineAlarmReceiver.EXTRA_TASK_NAME, name)
        i.putExtra(DeadlineAlarmReceiver.EXTRA_USERNAME, username)

        if (alarmRequestCode == -1) alarmRequestCode = abs( Random.nextInt() )
        return PendingIntent.getBroadcast(context, alarmRequestCode, i, 0)
    }

    fun makeTaskPending() {
        status = TaskStatus.PENDING
        statusHasChanged = true
    }
    fun makeTaskOnGoing() {
        status = TaskStatus.ON_GOING
        statusHasChanged = true
    }
    fun makeTaskCompleted() {
        status = TaskStatus.COMPLETED
        statusHasChanged = true
    }

    fun changeTaskDescription(newDescription: String) {
        description = newDescription
        descriptionHasChanged = true
    }

    /**
     * Update the task in the backend using API.
     * This function is blocking so do not call it in the UI thread
     */
    fun commitChanges(onCommitted: () -> Unit, onFailure: () -> Unit) {
        thread { try {
            doCommitChanges()
            onCommitted()
        } catch (e: Exception) {
            Log.e("MCC", e.toString())
            onFailure()
        }}
    }

    private fun doCommitChanges() {
        val apiTask = toApiModelTask()

        if (taskHasBeenCreated) {
            taskId = ApiClient.api.createTask(apiTask).id
        }
        if (statusHasChanged) {
            ApiClient.api.updateStatusOfTaskWithId(taskId, InlineObject1(status = apiTask.status))
        }
        if (descriptionHasChanged) {
            FirebaseDatabase.getInstance().reference.child("tasks")
                .child(taskId).child("description").setValue(description)
        }
        if (usersHaveBeenAssigned) {
            ApiClient.api.assignUsersToTask(taskId, InlineObject2(userIds = assignedUsersIds.toTypedArray()))
        }
    }

    /**
     * Convert task to API task model
     */
    private fun toApiModelTask(): com.mcc.g22.apiclient.models.Task {
        return com.mcc.g22.apiclient.models.Task(description = description,
            deadline = deadline.toString(),
            status = when(status) {
                TaskStatus.PENDING -> com.mcc.g22.apiclient.models.Status.pending
                TaskStatus.ON_GOING -> com.mcc.g22.apiclient.models.Status.ongoing
                TaskStatus.COMPLETED -> com.mcc.g22.apiclient.models.Status.completed
            },
            projectId = projectId)
    }
}