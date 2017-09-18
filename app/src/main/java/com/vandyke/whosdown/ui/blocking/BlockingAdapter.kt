package com.vandyke.whosdown.ui.blocking

import android.database.Cursor
import android.provider.ContactsContract
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vandyke.whosdown.R
import com.vandyke.whosdown.util.toLocalizedE164

class BlockingAdapter(val cursor: Cursor) : RecyclerView.Adapter<BlockingHolder>() {
    val nameCol = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
    val numberCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
    val uriCol = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)

    val database = FirebaseDatabase.getInstance().reference
    val auth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockingHolder {
        return BlockingHolder(View.inflate(parent.context, R.layout.holder_blocking, null))
    }

    override fun onBindViewHolder(holder: BlockingHolder, position: Int) {
        cursor.moveToPosition(position)
        holder.blocked = false
        holder.name.text = cursor.getString(nameCol)
        val number = cursor.getString(numberCol).toLocalizedE164(holder.itemView.context)
        holder.number = number
        if (number != null) {
            database.child("blocked")
                    .child(auth.currentUser!!.phoneNumber)
                    .child(number).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    holder.blocked = dataSnapshot.exists()
                }

                override fun onCancelled(p0: DatabaseError?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return cursor.count
    }

}