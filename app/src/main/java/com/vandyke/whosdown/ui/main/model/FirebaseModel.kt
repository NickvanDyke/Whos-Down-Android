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
import com.vandyke.whosdown.data.UserNode
import com.vandyke.whosdown.ui.main.viewmodel.ViewModel
import com.vandyke.whosdown.util.getCountryCode

class FirebaseModel(val viewModel: ViewModel) {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        database.getReference(".info/connected").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val connected = dataSnapshot.getValue(Boolean::class.java) ?: return
                viewModel.connected.set(connected)
            }

            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented")
            }
        })
    }

    val localNumberListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val downStatus = dataSnapshot.getValue(UserNode::class.java) ?: return
            println("new UserNode for local user: $downStatus")
            viewModel.down.set(downStatus.down)
            viewModel.message.set(downStatus.message)
        }

        override fun onCancelled(p0: DatabaseError?) {
            TODO("not implemented")
        }
    }

    fun setUserDown(down: Boolean) {
        database.reference.child(auth.currentUser!!.phoneNumber).child("down").setValue(down)
    }

    fun setUserMessage(msg: String) {
        database.reference.child(auth.currentUser!!.phoneNumber).child("message").setValue(msg)
    }

    /* adds a listener for the local user's status, and also loops through all of the phone's contacts,
        checks if there's an entry in the database for the phone number of each, and if there is, attaches a listener to that number's node */
    fun setListeners(context: Context) {
        database.reference.child(auth.currentUser!!.phoneNumber).addValueEventListener(localNumberListener)

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

        /* check the database for each of the contact's numbers, add listener to the node if it exists */
        for (i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            val name = cursor.getString(nameCol)
            val number = PhoneNumberUtils.formatNumberToE164(cursor.getString(numberCol), countryCode)
            if (number != null) {
                database.reference.child(number)?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val downStatus = dataSnapshot.getValue(UserNode::class.java) ?: return
                        val uri = Uri.parse("") // Uri.parse(cursor.getString(uriCol)) TODO: get proper contact thumbnail uri
                        val peep = Peep(name, uri, dataSnapshot.key, downStatus.down, downStatus.message)
                        println("peep update: $peep")
                        viewModel.updatePeeps(peep)
                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }
                })
            }
        }
    }
}