package com.mcc.g22

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.mcc.g22.apiclient.ApiClient
import com.mcc.g22.apiclient.models.InlineObject1
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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

    var description: String = ""
        private set

    var deadline: Date = Date()
        private set

    var status: TaskStatus = TaskStatus.PENDING
        private set

    private var assignedUsers: MutableSet<User> = mutableSetOf()
    private var taskId: String = ""

    private var statusHasChanged: Boolean = false
    private var taskHasBeenCreated: Boolean = false
    private var usersHaveBeenAssigned: Boolean = false
    private var descriptionHasChanged: Boolean = false

    companion object StaticFactories {
        @SuppressLint("SimpleDateFormat")
        private val deadlineDataFormat: DateFormat =
                                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

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
        fun getTaskFromDatabase(projectId: String, taskId: String, onTaskReady: (task: Task) -> Unit,
                       onFailure: () -> Unit) {

            val taskInDatabase = FirebaseDatabase.getInstance().reference.child("tasks")
                                                                .child(taskId)
            taskInDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(databaseError: DatabaseError) {
                    onFailure()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val d = dataSnapshot.child("description").value as String
                    val s = when (dataSnapshot.child("status").value) {
                        com.mcc.g22.apiclient.models.Task.Status.pending -> TaskStatus.PENDING
                        com.mcc.g22.apiclient.models.Task.Status.ongoing -> TaskStatus.ON_GOING
                        com.mcc.g22.apiclient.models.Task.Status.completed -> TaskStatus.COMPLETED
                        else -> {
                            onFailure()
                            return
                        }
                    }
                    val deadline = dataSnapshot.child("deadline").value as String

                    val task = createTask(projectId, d, deadlineDataFormat.parse(deadline)!!)

                    for (u in dataSnapshot.child("users").children) {
                        task.assignedUsers.add( User(u.key.toString()) )
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
        fun createTask(projectId: String, description: String, deadline: Date): Task {
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
        fun createTask(projectId: String, description: String, deadline: Date, assignedUsers: MutableSet<User>): Task {
            val t = Task()
            t.projectId = projectId
            t.description = description
            t.deadline = deadline
            t.assignedUsers = assignedUsers
            if (t.assignedUsers.isNotEmpty()) {
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
                com.mcc.g22.apiclient.models.Task.Status.pending -> TaskStatus.PENDING
                com.mcc.g22.apiclient.models.Task.Status.ongoing -> TaskStatus.ON_GOING
                com.mcc.g22.apiclient.models.Task.Status.completed -> TaskStatus.COMPLETED
            }
            t.deadline = deadlineDataFormat.parse(task.deadline)!!
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
                onTaskReady( createTask(projectId, it, Date()) )
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
                onTaskReady(createTask(projectId, it, Date()))
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
        assignedUsers.add(u)
        if (assignedUsers.size == 1 && status == TaskStatus.PENDING) {
            status = TaskStatus.ON_GOING
            statusHasChanged = true
        }
        usersHaveBeenAssigned = true
    }

    /**
     * Assigned many users to the task
     */
    fun assignUsers(users: Array<User>) {
        assignedUsers.addAll(users)
        if (assignedUsers.size >= 1 && status == TaskStatus.PENDING) {
            status = TaskStatus.ON_GOING
            statusHasChanged = true
        }
        usersHaveBeenAssigned = true
    }

    /**
     * Remove user from the task
     */
    fun removeUser(u: User) {
        assignedUsers.remove(u)
        if (assignedUsers.isEmpty() && status == TaskStatus.ON_GOING) {
            status = TaskStatus.PENDING
        }
    }

    /**
     * Return users assigned to this task.
     */
    fun getAssignedUsers(): Set<User> {
        return assignedUsers
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
    fun commitChanges() {
        val apiTask = toApiModelTask()

        if (taskHasBeenCreated) {
            taskId = ApiClient.api.createTask(projectId, apiTask).id
        }
        if (statusHasChanged) {
            ApiClient.api.updateStatusOfTaskWithId(projectId, taskId, apiTask)
        }
        if (descriptionHasChanged) {
            FirebaseDatabase.getInstance().reference.child("tasks")
                .child(taskId).child("description").setValue(description)
        }
        if (usersHaveBeenAssigned) {
            val usersIds = mutableListOf<String>()
            assignedUsers.forEach { usersIds.add(it.username) }
            ApiClient.api.assignUsersToTask(projectId, taskId, InlineObject1(usersIds.toTypedArray()))
        }
    }

    /**
     * Convert task to API task model
     */
    private fun toApiModelTask(): com.mcc.g22.apiclient.models.Task {
        return com.mcc.g22.apiclient.models.Task(description = description,
            deadline = deadlineDataFormat.format(deadline),
            status = when(status) {
                TaskStatus.PENDING -> com.mcc.g22.apiclient.models.Task.Status.pending
                TaskStatus.ON_GOING -> com.mcc.g22.apiclient.models.Task.Status.ongoing
                TaskStatus.COMPLETED -> com.mcc.g22.apiclient.models.Task.Status.completed
            })
    }
}