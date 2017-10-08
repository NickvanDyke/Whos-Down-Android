package com.vandyke.whosdown.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

fun currentUser(db: FirebaseDatabase = FirebaseDatabase.getInstance(), auth: FirebaseAuth = FirebaseAuth.getInstance()): DatabaseReference
        = db.reference.child("users").child(auth.currentUser!!.phoneNumber)

fun user(number: String, db: FirebaseDatabase = FirebaseDatabase.getInstance()): DatabaseReference
        = db.reference.child("users").child(number)