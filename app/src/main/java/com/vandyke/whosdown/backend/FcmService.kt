package com.vandyke.whosdown.backend

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vandyke.whosdown.R
import com.vandyke.whosdown.ui.contact.view.ContactActivity
import com.vandyke.whosdown.ui.main.view.MainActivity
import com.vandyke.whosdown.util.getContactName


class FcmService : FirebaseMessagingService() {

    // TODO: don't show notification if app is in the foreground
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

            if (people.size == 1) {
                val contactIntent = Intent(this, ContactActivity::class.java)
                contactIntent.putExtra("phoneNumber", phoneNumber)

                val pendingIntent = TaskStackBuilder.create(this).addNextIntent(Intent(this, MainActivity::class.java))
                        .addNextIntent(contactIntent).getPendingIntent(100, 0)

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