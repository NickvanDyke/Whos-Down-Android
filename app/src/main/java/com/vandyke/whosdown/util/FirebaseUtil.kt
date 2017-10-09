package com.vandyke.whosdown.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

fun currentUser(db: FirebaseDatabase = FirebaseDatabase.getInstance(), auth: FirebaseAuth = FirebaseAuth.getInstance()): DatabaseReference
        = db.reference.child("users").child(auth.currentUser!!.phoneNumber)

fun user(number: String, db: FirebaseDatabase = FirebaseDatabase.getInstance()): DatabaseReference
        = db.reference.child("users").child(number)

fun DatabaseReference.addValueEventListener(onDataChange: (DataSnapshot) -> Unit, onCancelled: (DatabaseError) -> Unit) {
    this.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            onDataChange(dataSnapshot)
        }

        override fun onCancelled(error: DatabaseError) {
            onCancelled(error)
        }
    })
}