package com.vandyke.whosdown.ui.contact.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vandyke.whosdown.ui.contact.viewmodel.ContactViewModel
import com.vandyke.whosdown.util.addValueEventListener

class ContactModel(val viewModel: ContactViewModel, val phoneNumber: String) {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        database.reference.child("users").child(phoneNumber).child("subscribers").child(auth.currentUser!!.phoneNumber).addValueEventListener({
            val value = it.getValue(Boolean::class.java) ?: return@addValueEventListener
            viewModel.subscribed.set(value)
        }, {

        })

        database.reference.child("users").child(auth.currentUser!!.phoneNumber).child("blocked").child(phoneNumber).addValueEventListener({
            val value = it.getValue(Boolean::class.java) ?: return@addValueEventListener
            viewModel.blocked.set(value)
        }, {

        })
    }

    fun setSubscribed(subscribed: Boolean) {
        if (subscribed)
            database.reference.child("users").child(phoneNumber).child("subscribers").child(auth.currentUser!!.phoneNumber).setValue(true)
        else
            database.reference.child("users").child(phoneNumber).child("subscribers").child(auth.currentUser!!.phoneNumber).removeValue()
    }

    fun setBlocked(blocked: Boolean) {
        if (blocked)
            database.reference.child("users").child(auth.currentUser!!.phoneNumber).child("blocked").child(phoneNumber).setValue(true)
        else
            database.reference.child("users").child(auth.currentUser!!.phoneNumber).child("blocked").child(phoneNumber).removeValue()
    }
}