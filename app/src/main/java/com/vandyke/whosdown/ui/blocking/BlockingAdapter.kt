package com.vandyke.whosdown.ui.blocking

import android.database.Cursor
import android.provider.ContactsContract
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

class BlockingAdapter(val cursor: Cursor) : RecyclerView.Adapter<BlockingHolder>() {
    val nameCol = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
    val numberCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
    val uriCol = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)

    override fun onBindViewHolder(holder: BlockingHolder?, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): BlockingHolder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getItemCount(): Int {
        return cursor.count
    }

}