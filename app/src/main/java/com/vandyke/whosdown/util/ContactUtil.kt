/*
 * Copyright (c) 2017 Nicholas van Dyke
 * All rights reserved.
 */

package com.vandyke.whosdown.util

import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract

fun ContentResolver.getContactName(phoneNumber: String): String {
    val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))

    val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY)

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