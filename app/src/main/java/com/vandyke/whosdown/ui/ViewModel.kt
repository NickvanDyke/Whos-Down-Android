package com.vandyke.whosdown.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.CursorLoader
import android.databinding.*
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.vandyke.whosdown.auth
import com.vandyke.whosdown.data.DownStatus
import com.vandyke.whosdown.data.Peep
import com.vandyke.whosdown.database
import com.vandyke.whosdown.util.GenUtil



class ViewModel(application: Application) : AndroidViewModel(application) {
    val down: ObservableBoolean = ObservableBoolean(false)
    val localUserMessage: ObservableField<String> = ObservableField("")
    val peeps: ObservableList<Peep> = ObservableArrayList<Peep>()

    private val PROJECTION = arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)

    init {
        database.child(auth.currentUser?.phoneNumber).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val downStatus = dataSnapshot.getValue(DownStatus::class.java) ?: return
                println("new DownStatus for local user: $downStatus")
                down.set(downStatus.down)
                localUserMessage.set(downStatus.message)
            }

            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented")
            }
        })

        localUserMessage.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                val msg = (sender as ObservableField<String>).get()
                database.child(auth.currentUser?.phoneNumber).child("message").setValue(msg)
            }
        })

        val cursorLoader = CursorLoader(application,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

        val cursor = cursorLoader.loadInBackground()
        val nameCol = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        val numberCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val uriCol = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)

        val countryCode = GenUtil.getCountryCode(application).toUpperCase()

        /* check the database for each of the contact's numbers */
        for (i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            val name = cursor.getString(nameCol)
            val number = PhoneNumberUtils.formatNumberToE164(cursor.getString(numberCol), countryCode)
            if (number != null && database.child(number) != null) { // if number is valid and in the database
                database.child(number).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val downStatus = dataSnapshot.getValue(DownStatus::class.java) ?: return
                        val uri = Uri.parse("") // Uri.parse(cursor.getString(uriCol)) TODO: get proper contact thumbnail uri
                        val peep = Peep(name, uri, dataSnapshot.key, downStatus.down, downStatus.message)
                        println("peep update: $peep")
                        updatePeeps(peep)
                    }

                    override fun onCancelled(p0: DatabaseError) {

                    }
                })

            }
        }
    }

    fun onDownSwitched(down: Boolean) {
        database.child(auth.currentUser?.phoneNumber).setValue(DownStatus(down, localUserMessage.get()))
    }

    fun updatePeeps(updatedPeep: Peep) {
        if (updatedPeep.down) {
            /* check if updatedPeep already exists in the list, and if it does, update it's message if necessary, then return */
            peeps.forEachIndexed { i, peep ->
                if (peep.number == updatedPeep.number) {
                    if (peep.message == updatedPeep.message) {
                        return
                    } else {
                        peeps[i] = updatedPeep /* need to set it to the updatedPeep, changing the existing one won't trigger the observable callback */
                        return
                    }
                }
            }
            /* if the updatedPeep isn't already in the list, it's added at the beginning */
            peeps.add(0, updatedPeep)
        } else { /* else the peep isn't down and will be removed from the list of down peeps */
            val index = peeps.indexOfFirst { it.number == updatedPeep.number }
            if (index != -1)
                peeps.removeAt(index) /* need to remove using removeAt, otherwise the observable callback method isn't called... */
        }
    }
}