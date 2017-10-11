package com.vandyke.whosdown.backend

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.vandyke.whosdown.util.currentUser

class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        if (FirebaseAuth.getInstance().currentUser == null)
            return
        currentUser()
                .child("notificationToken")
                .setValue(FirebaseInstanceId.getInstance().token)
    }
}