/*
 * Copyright (c) 2017 Nicholas van Dyke
 * All rights reserved.
 */

package com.vandyke.whosdown.backend

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vandyke.whosdown.R
import com.vandyke.whosdown.ui.main.view.MainActivity
import com.vandyke.whosdown.util.Intents
import com.vandyke.whosdown.util.getContactName


class FcmService : FirebaseMessagingService() {

    // TODO: don't show notification if app is in the foreground, and add call/text actions to 1-person notifications
    // maybe also use multi-line stuff in the notifications
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val phoneNumber = remoteMessage.data["phoneNumber"] ?: return
            val message = remoteMessage.data["message"] ?: return
            val name = contentResolver.getContactName(phoneNumber)

            val people = prefs.getStringSet("notifications", mutableSetOf())
            people.add(name)
            val builder = NotificationCompat.Builder(applicationContext)
                    .setSmallIcon(R.drawable.whos_down_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                    .setDeleteIntent(PendingIntent.getBroadcast(this, 0,
                            Intent(this, NotificationDismissedReceiver::class.java), 0))

            if (people.size == 1) {
                val pendingIntent = PendingIntent.getActivities(this, 0,
                        arrayOf(Intent(this, MainActivity::class.java), Intents.contactActivity(this, phoneNumber)),
                        PendingIntent.FLAG_UPDATE_CURRENT)

                builder.setContentTitle("$name is down!")
                        .setContentText(message)
                        .setContentIntent(pendingIntent)
            } else {
                val intent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT)

                var text = ""
                people.forEachIndexed { index, s ->
                    text += s
                    if (index < people.size - 1)
                        text += ", "
                }

                builder.setContentTitle("${people.size} people are down!")
                        .setContentText(text)
                        .setContentIntent(intent)
            }

            prefs.edit().putStringSet("notifications", people).apply()
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, builder.build())
        }
    }
}