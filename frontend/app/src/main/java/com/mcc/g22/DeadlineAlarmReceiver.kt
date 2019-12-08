package com.mcc.g22

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class DeadlineAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_USERNAME: String = "EXTRA_USERNAME"
        const val EXTRA_TASK_NAME: String = "EXTRA_TASK_NAME"
    }

    override fun onReceive(context: Context, intent: Intent) {

        val username = intent.extras!!.get(EXTRA_USERNAME) as String
        val taskName = intent.extras!!.get(EXTRA_TASK_NAME) as String

        var importance = 3 // Importance default
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            importance = NotificationManager.IMPORTANCE_DEFAULT
        }
        val nh = NotificationHelper(context,
            context.getString(R.string.deadline_channel_name),
            context.getString(R.string.deadline_channel_description),
            importance)

        // TODO add proper icon
        // TODO add proper onTapAction
        val notificationTitle = context.getString(R.string.task_deadline_notif_title)
        val notificationText = username + ", " +
                context.getString(R.string.task_deadline_notif_description) + " " + taskName
        nh.showNotification(notificationTitle, notificationText)
    }
}
