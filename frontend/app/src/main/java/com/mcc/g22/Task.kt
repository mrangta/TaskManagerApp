package com.mcc.g22

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Task to be added to a project
 */
class Task {

    enum class TaskStatus {
        PENDING, ON_GOING, COMPLETED
    }

    var description: String = ""

    var deadline: Date = Date()

    var status: TaskStatus = TaskStatus.PENDING

    private var assignedUsers: MutableSet<User> = mutableSetOf()

    companion object StaticFactories {
        @SuppressLint("SimpleDateFormat")
        private val deadlineDataFormat: DateFormat =
                                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

        /**
         * Create pending task.
         */
        fun createTask(description: String, deadline: Date): Task {
            val t = Task()
            t.description = description
            t.deadline = deadline
            return t
        }

        /**
         * Create task with assigned users. If number of the users is > 0, task in on-going
         */
        fun createTask(description: String, deadline: Date, assignedUsers: MutableSet<User>): Task {
            val t = Task()
            t.description = description
            t.deadline = deadline
            t.assignedUsers = assignedUsers
            if (t.assignedUsers.isNotEmpty()) {
                t.status = TaskStatus.ON_GOING
            }
            return t
        }

        /**
         * Create task from the API model of task object
         */
        fun createTask(task: com.mcc.g22.apiclient.models.Task): Task {
            val t = Task()
            t.description = task.description
            t.status = when(task.status) {
                com.mcc.g22.apiclient.models.Task.Status.pending -> TaskStatus.PENDING
                com.mcc.g22.apiclient.models.Task.Status.ongoing -> TaskStatus.ON_GOING
                com.mcc.g22.apiclient.models.Task.Status.completed -> TaskStatus.COMPLETED
            }
            t.deadline = deadlineDataFormat.parse(task.deadline)!!
            return t
        }

        /**
         * Convert image to pending tasks.
         * Description of the task is the text from the image.
         *
         * @param bitmap image to convert to task
         * @param onTaskReady function to be called when task in read to use
         * @param onFailure function to be called when text cannot be recognized
         */
        fun createTask(bitmap: Bitmap,
                       onTaskReady: (task: Task) -> Unit, onFailure: () -> Unit) {

            val image = FirebaseVisionImage.fromBitmap(bitmap)
            runTextDetector(image, {
                onTaskReady( createTask(it, Date()) )
            }, onFailure)
        }

        /**
         * Convert image to pending tasks.
         * Description of the task is the text from the image.
         *
         * @param context application context
         * @param imageUri image to convert to task
         * @param onTaskReady function to be called when task in read to use
         * @param onFailure function to be called when text cannot be recognized
         */
        fun createTask(context: Context, imageUri: Uri,
                       onTaskReady: (task: Task) -> Unit, onFailure: () -> Unit) {

            val image = FirebaseVisionImage.fromFilePath(context, imageUri)
            runTextDetector(image, {
                onTaskReady(createTask(it, Date()))
            }, onFailure)
        }

        /**
         * Convert image to pending tasks.
         * Description of the task is the text from the image.
         *
         * @param context application context
         * @param imageFile image to convert to task
         * @param onTaskReady function to be called when task in read to use
         * @param onFailure function to be called when text cannot be recognized
         */
        fun createTask(context: Context, imageFile: File,
                       onTaskReady: (task: Task) -> Unit, onFailure: () -> Unit) {
            createTask(context, Uri.fromFile(imageFile), onTaskReady, onFailure)
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

    fun assignUser(u: User) {
        assignedUsers.add(u)
        if (assignedUsers.size == 1 && status == TaskStatus.PENDING) {
            status = TaskStatus.ON_GOING
        }
    }

    fun removeUser(u: User) {
        assignedUsers.remove(u)
        if (assignedUsers.isEmpty() && status == TaskStatus.ON_GOING) {
            status = TaskStatus.PENDING
        }
    }

    fun getAssignedUsers(): MutableSet<User> {
        return assignedUsers
    }

    fun toApiModelTask(): com.mcc.g22.apiclient.models.Task {
        return com.mcc.g22.apiclient.models.Task(description = description,
            deadline = deadlineDataFormat.format(deadline),
            status = when(status) {
                TaskStatus.PENDING -> com.mcc.g22.apiclient.models.Task.Status.pending
                TaskStatus.ON_GOING -> com.mcc.g22.apiclient.models.Task.Status.ongoing
                TaskStatus.COMPLETED -> com.mcc.g22.apiclient.models.Task.Status.completed
            })
    }
}