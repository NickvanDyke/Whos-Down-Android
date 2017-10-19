package com.vandyke.whosdown.backend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vandyke.whosdown.util.clearNotifications

class NotificationDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        clearNotifications(context)
    }
}