package com.vandyke.whosdown.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import com.vandyke.whosdown.ui.contact.view.ContactActivity

object Intents {
    fun text(number: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("sms:" + number)
        return intent
    }

    fun call(number: String) = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null))

    fun viewContact(lookupKey: String) = Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey))

    fun contactActivity(context: Context, phoneNumber: String): Intent {
        val intent = Intent(context, ContactActivity::class.java)
        intent.putExtra("phoneNumber", phoneNumber)
        return intent
    }
}