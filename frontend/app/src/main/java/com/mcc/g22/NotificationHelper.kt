package com.mcc.g22

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random

class NotificationHelper(
    private val context: Context,
    private val notificationChannelName: String,
    private val notificationChannelDescription: String,
    importance: Int) {

    init {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notificationChannelName, notificationChannelName,
                                                importance).apply {

                description = notificationChannelDescription
            }
            // TODO configure vibration, lights, sound, ...
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create and show notification.
     * @param title
     * @param text
     * @param icon R.drawable with notification icon
     * @param onTapAction PendingIntent to start when the notification is tapped
     * @param priority NotificationCompat priority
     * @return notification ID
     */
    fun showNotification(title: String, text: String,
                         icon: Int = R.drawable.ic_launcher_background,
                         onTapAction: PendingIntent? = null,
                         priority: Int = NotificationCompat.PRIORITY_DEFAULT): Int {

        // Build notification to show
        val builder = NotificationCompat.Builder(context, notificationChannelName)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(priority)
        if (onTapAction != null) builder.setContentIntent(onTapAction)

        // Show notification
        with(NotificationManagerCompat.from(context)) {
            // first argument is notificationId is a unique int for each notification
            // because notification won't be changed, this is not stored and it is just random
            // number
            val notificationId = Random.nextInt()
            notify(notificationId, builder.build())
            return notificationId
        }
    }
}
