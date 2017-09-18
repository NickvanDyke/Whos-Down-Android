package com.vandyke.whosdown.ui.blocking

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.vandyke.whosdown.R

class BlockingHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    val pic = itemView.findViewById<ImageView>(R.id.contactPic)
    val name = itemView.findViewById<TextView>(R.id.contactName)
    var number: String? = null
    var blocked = false
        set(value) {
            if (value)
                itemView.setBackgroundColor(blockedColor)
            else
                itemView.setBackgroundColor(Color.WHITE)
            field = value
        }

    init {
        itemView.setOnClickListener(this)
    }

    /* note that blocked (and therefore the color) doesn't change until the data is actually changed in the database */
    override fun onClick(view: View) {
        if (number == null)
            return
        if (!blocked) {
            FirebaseDatabase.getInstance().reference.child("blocked")
                    .child(FirebaseAuth.getInstance().currentUser!!.phoneNumber)
                    .child(number)
                    .setValue(true, { databaseError, databaseReference -> blocked = true })
        } else {
            FirebaseDatabase.getInstance().reference.child("blocked")
                    .child(FirebaseAuth.getInstance().currentUser!!.phoneNumber)
                    .child(number)
                    .removeValue({ databaseError, databaseReference -> blocked = false })
        }
    }

    companion object {
        val blockedColor = Color.parseColor("#ff8e8e")
    }
}