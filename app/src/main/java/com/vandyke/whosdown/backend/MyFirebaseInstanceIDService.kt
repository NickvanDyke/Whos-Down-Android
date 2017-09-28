package com.vandyke.whosdown.backend

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null)
            return
        FirebaseDatabase.getInstance().reference.child("users")
                .child(auth.currentUser!!.phoneNumber)
                .child("notificationToken")
                .setValue(FirebaseInstanceId.getInstance().token)
    }
}