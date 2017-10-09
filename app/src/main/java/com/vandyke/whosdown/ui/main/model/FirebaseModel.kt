package com.vandyke.whosdown.ui.main.model

import android.content.Context
import android.content.CursorLoader
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.vandyke.whosdown.backend.data.Peep
import com.vandyke.whosdown.backend.data.UserStatus
import com.vandyke.whosdown.backend.data.UserStatusUpdate
import com.vandyke.whosdown.ui.main.viewmodel.MainViewModel
import com.vandyke.whosdown.util.addValueEventListener
import com.vandyke.whosdown.util.currentUser
import com.vandyke.whosdown.util.getCountryCode

class FirebaseModel(val viewModel: MainViewModel) {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val listeners = mutableMapOf<String, ValueEventListener>()

    init {
        database.getReference(".info/connected").addValueEventListener({
            val connected = it.getValue(Boolean::class.java) ?: return@addValueEventListener
            viewModel.connected.set(connected)
        }, {
            TODO("not implemented")
        })
    }

    val userListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val userStatus = dataSnapshot.getValue(UserStatus::class.java) ?: return
            println("new UserStatus for local user: $userStatus")
            /* need to compare to current values, otherwise it'll freak out */
                viewModel.down.set(userStatus.down)
                viewModel.message.set(userStatus.message)
        }

        override fun onCancelled(error: DatabaseError?) {
            TODO("not implemented")
        }
    }

    fun setUserStatus(status: UserStatusUpdate) {
        currentUser(database, auth).child("status").setValue(status)
    }

    fun setUserDown(down: Boolean) {
        currentUser(database, auth).child("status").child("down").setValue(down)
        if (down)
            setUserTimestamp()
    }

    fun setUserMessage(msg: String) {
        currentUser(database, auth).child("status").child("message").setValue(msg)
        setUserTimestamp()
    }

    fun setUserTimestamp() {
        currentUser(database, auth).child("status").child("timestamp").setValue(ServerValue.TIMESTAMP)
    }

    /* adds a listener to the user node for the current local user */
    fun setUserDbListener() {
        currentUser(database, auth).child("status").addValueEventListener(userListener)
    }

    fun setDbListeners(context: Context) {
        val cursorLoader = CursorLoader(context,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                null,
                null,
                null)

        val cursor = cursorLoader.loadInBackground()
        val numberCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        val countryCode = context.getCountryCode()

        /* add a listener to the user number of each contact's phone number */
        for (i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            val number = PhoneNumberUtils.formatNumberToE164(cursor.getString(numberCol), countryCode)
            if (number != null && !listeners.containsKey(number)) {
                listeners.put(number, database.reference.child("users").child(number).child("status").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val userStatus = dataSnapshot.getValue(UserStatus::class.java)
                        if (userStatus == null) { /* cancel listeners on nodes that don't already exist. Probably not worth the resources to monitor them */
                            database.reference.child("users").child(number).child("status").removeEventListener(this)
                            listeners.remove(number)
                            return
                        }
                        val peep = Peep(number, userStatus.down, userStatus.message, userStatus.timestamp)
                        println("peep update: $peep")
                        viewModel.updatePeeps(peep)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        listeners.remove(number)
                        viewModel.updatePeeps(Peep(number, false, "", 0))
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