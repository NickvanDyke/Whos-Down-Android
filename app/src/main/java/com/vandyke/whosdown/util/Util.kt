package com.vandyke.whosdown.util

import android.content.ContentResolver
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.databinding.BaseObservable
import android.databinding.Observable
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener


fun Context.getCountryCode(): String {
    return (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).networkCountryIso.toUpperCase()
}

fun String.toLocalizedE164(context: Context): String? {
    return PhoneNumberUtils.formatNumberToE164(this, context.getCountryCode())
}

fun phoneNumberUri(number: String) = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))

fun ContentResolver.getContactName(phoneNumber: String): String {
    val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))

    val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

    var contactName = ""
    val cursor = this.query(uri, projection, null, null, null)

    if (cursor != null) {
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(0)
        }
        cursor.close()
    }

    return contactName
}

fun Long.toTimePassedString(): String {
    val difference = System.currentTimeMillis() - this
    return when {
        difference > 86400000 -> (difference / 86400000).toString() + "d"
        difference > 3600000 -> (difference / 3600000).toString() + "h"
        difference > 60000 -> (difference / 60000).toString() + "m"
        else -> "just now"
    }
}

fun BaseObservable.addOnPropertyChangedListener(function: (Observable, Int) -> Unit) {
    this.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
            function(sender, propertyId)
        }
    })
}

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