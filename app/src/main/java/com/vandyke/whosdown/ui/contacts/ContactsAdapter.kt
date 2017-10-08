package com.vandyke.whosdown.ui.contacts

import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vandyke.whosdown.R
import com.vandyke.whosdown.util.toLocalizedE164

class ContactsAdapter(val cursor: Cursor) : RecyclerView.Adapter<ContactHolder>() {
    val nameCol = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
    val numberCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
    val uriCol = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)

    val database = FirebaseDatabase.getInstance().reference
    val auth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        return ContactHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_contact, parent, false))
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        cursor.moveToPosition(position)
        holder.name.text = cursor.getString(nameCol)
        val number = cursor.getString(numberCol).toLocalizedE164(holder.itemView.context)
        holder.number = number
        val imageUriString = cursor.getString(uriCol)
        if (imageUriString != null)
            holder.pic.setImageURI(Uri.parse(imageUriString))
        else
            holder.pic.setImageResource(R.drawable.person_placeholder)
    }

    override fun getItemCount(): Int {
        return cursor.count
    }

}