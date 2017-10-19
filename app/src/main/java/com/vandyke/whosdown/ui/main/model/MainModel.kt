package com.vandyke.whosdown.ui.main.model

import android.content.Context
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vandyke.whosdown.backend.data.Peep
import com.vandyke.whosdown.backend.data.UserStatus
import com.vandyke.whosdown.backend.data.UserStatusUpdate
import com.vandyke.whosdown.ui.main.viewmodel.MainViewModel
import com.vandyke.whosdown.util.addValueEventListener
import com.vandyke.whosdown.util.currentUser
import com.vandyke.whosdown.util.getCountryCode
import com.vandyke.whosdown.util.user

class MainModel(val viewModel: MainViewModel) {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val listeners = mutableMapOf<String, ValueEventListener>()

    init {
        database.getReference(".info/connected").addValueEventListener({
            val connected = it.getValue(Boolean::class.java) ?: return@addValueEventListener
            viewModel.connected.set(connected)
        }, {

        })
    }

    val userListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val userStatus = dataSnapshot.getValue(UserStatus::class.java) ?: return
            println("new UserStatus for local user: $userStatus")
            viewModel.modelMakingChanges = true
            viewModel.message.set(userStatus.message)
            viewModel.down.set(userStatus.down)
            viewModel.modelMakingChanges = false
        }

        override fun onCancelled(error: DatabaseError) {

        }
    }

    fun setUserStatus(status: UserStatusUpdate) {
        currentUser(database, auth).child("status").setValue(status)
    }

    /* adds a listener to the user node for the current local user */
    fun setUserDbListener() {
        currentUser(database, auth).child("status").addValueEventListener(userListener)
    }

    fun setDbListeners(context: Context) {
        val cursor = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                        ContactsContract.Contacts.LOOKUP_KEY),
                null,
                null,
                null) ?: return

        val numberCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val nameCol = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        val photoUriCol = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)
        val lookupCol = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)

        val countryCode = context.getCountryCode()

        /* add a listener to the user phoneNumber of each contact's phone number */
        for (i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            val number = PhoneNumberUtils.formatNumberToE164(cursor.getString(numberCol), countryCode)
            val name = cursor.getString(nameCol)
            val lookupKey = cursor.getString(lookupCol)
            val photoUri = cursor.getString(photoUriCol)
            if (number != null && !listeners.containsKey(number)) {
                listeners.put(number, user(number, database).child("status").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val userStatus = dataSnapshot.getValue(UserStatus::class.java)
                        if (userStatus == null) { /* cancel listeners on nodes that don't already exist. Probably not worth the resources to monitor them */
                            user(number, database).child("status").removeEventListener(this)
                            listeners.remove(number)
                            return
                        }
                        val peep = Peep(number, name, lookupKey, photoUri, userStatus.down, userStatus.message, userStatus.timestamp)
                        println("peep update: $peep")
                        viewModel.updatePeeps(peep)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        listeners.remove(number)
                        viewModel.removePeep(number)
                    }
                }))
            }
        }
        cursor.close()
    }

    fun removeAllDbListeners() {
        database.reference.child("users").child(auth.currentUser!!.phoneNumber).child("status").removeEventListener(userListener)
        listeners.forEach { database.reference.child("users").child(it.key).child("status").removeEventListener(it.value) }
    }
}