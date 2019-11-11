package com.mcc.g22

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import java.io.File
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

/**
 * Task to be added to a project
 */
class Task {

    enum class TaskStatus {
        PENDING, ON_GOING, COMPLETED
    }

    private var alarmRequestCode: Int = -1

    var name: String = ""

    var description: String = ""

    var deadline: Date = Date()

    var status: TaskStatus = TaskStatus.PENDING

    private var assignedUsers: MutableSet<User> = mutableSetOf()

    companion object StaticFactories {

        /**
         * Create pending task.
         */
        fun createTask(name: String, description: String, deadline: Date): Task {
            val t = Task()
            t.name = name
            t.description = description
            t.deadline = deadline
            return t
        }

        /**
         * Create task with assigned users. If number of the users is > 0, task in on-going
         */
        fun createTask(name: String, description: String, deadline: Date, assignedUsers: MutableSet<User>): Task {
            val t = Task()
            t.name = name
            t.description = description
            t.deadline = deadline
            t.assignedUsers = assignedUsers
            if (t.assignedUsers.isNotEmpty()) {
                t.status = TaskStatus.ON_GOING
            }
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
                onTaskReady( createTask("Scanned task", it, Date()) )
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
                onTaskReady(createTask("Scanned task", it, Date()))
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

    /**
     * Set reminder of deadline for the task.
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
            deadline.time - triggerBefore,
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

    fun getAssignedUsers(): MutableSet<User> {
        return assignedUsers
    }
}