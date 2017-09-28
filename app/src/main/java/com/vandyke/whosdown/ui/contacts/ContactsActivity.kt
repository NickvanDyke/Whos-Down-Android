package com.vandyke.whosdown.ui.contacts

import android.app.Activity
import android.content.CursorLoader
import android.os.Bundle
import android.provider.ContactsContract
import com.vandyke.whosdown.R
import kotlinx.android.synthetic.main.activity_contacts.*

class ContactsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        actionBar.title = "Contacts"

        val cursorLoader = CursorLoader(this,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI),
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

        contactsList.adapter = ContactAdapter(cursorLoader.loadInBackground())
    }
}
