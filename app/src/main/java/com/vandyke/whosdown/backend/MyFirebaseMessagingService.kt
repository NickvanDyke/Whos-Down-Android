package com.vandyke.whosdown.backend

import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.vandyke.whosdown.R
import com.vandyke.whosdown.util.getContactName


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            val phoneNumber = remoteMessage.data["phoneNumber"] ?: return
            val message = remoteMessage.data["message"] ?: return

            val builder = NotificationCompat.Builder(applicationContext)
                    .setSmallIcon(R.drawable.whos_down_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                    .setContentTitle("${contentResolver.getContactName(phoneNumber)} is down!")
                    .setContentText(message)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, builder.build())
        }
    }
}