package com.vandyke.whosdown.ui.contact.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vandyke.whosdown.backend.data.UserStatus
import com.vandyke.whosdown.ui.contact.viewmodel.ContactViewModel
import com.vandyke.whosdown.util.addValueEventListener
import com.vandyke.whosdown.util.currentUser
import com.vandyke.whosdown.util.user

class ContactModel(val viewModel: ContactViewModel, val phoneNumber: String) {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        database.getReference(".info/connected").addValueEventListener({
            val connected = it.getValue(Boolean::class.java) ?: return@addValueEventListener
            viewModel.connected.set(connected)
        }, {

        })

        user(phoneNumber, database).child("status").addValueEventListener({
            val value = it.getValue(UserStatus::class.java) ?: return@addValueEventListener
            viewModel.down.set(value.down)
            viewModel.message.set(value.message)
        }, {

        })

        user(phoneNumber, database).child("subscribers").child(auth.currentUser!!.phoneNumber).addValueEventListener({
            val value = it.getValue(Boolean::class.java) ?: false
            viewModel.subscribed.set(value)
        }, {

        })

        currentUser(database, auth).child("blocked").child(phoneNumber).addValueEventListener({
            val value = it.getValue(Boolean::class.java) ?: false
            viewModel.blocked.set(value)
        }, {

        })
    }

    fun setSubscribed(subscribed: Boolean) {
        val ref = user(phoneNumber, database).child("subscribers").child(auth.currentUser!!.phoneNumber)
        if (subscribed)
            ref.setValue(true)
        else
            ref.removeValue()
    }

    fun setBlocked(blocked: Boolean) {
        val ref = currentUser(database, auth).child("blocked").child(phoneNumber)
        if (blocked)
            ref.setValue(true)
        else
            ref.removeValue()
    }
}