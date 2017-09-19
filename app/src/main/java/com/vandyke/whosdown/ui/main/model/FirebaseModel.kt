package com.vandyke.whosdown.ui.main.model

import android.content.Context
import android.content.CursorLoader
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vandyke.whosdown.data.Peep
import com.vandyke.whosdown.data.UserStatus
import com.vandyke.whosdown.ui.main.viewmodel.ViewModel
import com.vandyke.whosdown.util.getCountryCode

class FirebaseModel(val viewModel: ViewModel) {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val listeners = mutableMapOf<String, ValueEventListener>()

    init {
        database.getReference(".info/connected").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val connected = dataSnapshot.getValue(Boolean::class.java) ?: return
                viewModel.connected.set(connected)
            }

            override fun onCancelled(error: DatabaseError?) {
                TODO("not implemented")
            }
        })
    }

    val localNumberListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val userStatus = dataSnapshot.getValue(UserStatus::class.java) ?: return
            println("new UserStatus for local user: $userStatus")
            viewModel.down.set(userStatus.down)
            viewModel.message.set(userStatus.message)
        }

        override fun onCancelled(error: DatabaseError?) {
            TODO("not implemented")
        }
    }

    fun setUserDown(down: Boolean) {
        database.reference.child("users").child(auth.currentUser!!.phoneNumber).child("down").setValue(down)
    }

    fun setUserMessage(msg: String) {
        database.reference.child("users").child(auth.currentUser!!.phoneNumber).child("message").setValue(msg)
    }

    /* adds a listener to the user node for the current local user */
    fun setUserDbListener() {
        database.reference.child("users").child(auth.currentUser!!.phoneNumber).addValueEventListener(localNumberListener)
    }

    fun setDbListeners(context: Context) {
        val cursorLoader = CursorLoader(context,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI),
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

        val cursor = cursorLoader.loadInBackground()
        val nameCol = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        val numberCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val uriCol = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)

        val countryCode = context.getCountryCode()

        /* add a listener to the user number of each contact's phone number */
        for (i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            val name = cursor.getString(nameCol)
            val number = PhoneNumberUtils.formatNumberToE164(cursor.getString(numberCol), countryCode)
            if (number != null && !listeners.containsKey(number)) {
                listeners.put(number, database.reference.child("users").child(number).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!dataSnapshot.exists()) {/* cancel listeners on nodes that don't already exist. Not worth the resources to monitor them */
                            database.reference.child("users").child(number).removeEventListener(this)
                            listeners.remove(number)
                        }
                        val userStatus = dataSnapshot.getValue(UserStatus::class.java) ?: return
                        val uri = Uri.parse("") // Uri.parse(cursor.getString(uriCol)) TODO: get proper contact thumbnail uri
                        val peep = Peep(name, uri, dataSnapshot.key, userStatus.down, userStatus.message)
                        println("peep update: $peep")
                        viewModel.updatePeeps(peep)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        listeners.remove(number)
                        viewModel.updatePeeps(Peep(name, Uri.parse(""), number, false, ""))
                    }
                }))
            }
        }
    }

    fun removeAllDbListeners() {
        database.reference.child("users").child(auth.currentUser!!.phoneNumber).removeEventListener(localNumberListener)
        listeners.forEach { database.reference.child("users").child(it.key).removeEventListener(it.value) }
    }
}