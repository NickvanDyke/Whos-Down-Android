package com.vandyke.whosdown.ui

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
import com.vandyke.whosdown.data.DownStatus
import com.vandyke.whosdown.data.Peep
import com.vandyke.whosdown.util.GenUtil

class FirebaseModel(val viewModel: ViewModel) {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val otherNumbersListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val downStatus = dataSnapshot.getValue(DownStatus::class.java) ?: return
            println("new DownStatus for local user: $downStatus")
            viewModel.down.set(downStatus.down)
            viewModel.localUserMessage.set(downStatus.message)
        }

        override fun onCancelled(p0: DatabaseError?) {
            TODO("not implemented")
        }
    }

    fun setListeners(context: Context) {
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

        val countryCode = GenUtil.getCountryCode(context).toUpperCase()

        /* check the database for each of the contact's numbers */
        for (i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            val name = cursor.getString(nameCol)
            val number = PhoneNumberUtils.formatNumberToE164(cursor.getString(numberCol), countryCode)
            if (number != null) {
                database.reference.child(number)?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val downStatus = dataSnapshot.getValue(DownStatus::class.java) ?: return
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