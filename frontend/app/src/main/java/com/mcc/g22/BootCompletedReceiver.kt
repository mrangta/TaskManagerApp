package com.mcc.g22

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationsService.startNotificationService(context, User.getRegisteredUser()!!.username)
    }
}
