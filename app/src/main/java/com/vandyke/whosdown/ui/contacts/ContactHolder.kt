package com.vandyke.whosdown.ui.contacts

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.vandyke.whosdown.R
import com.vandyke.whosdown.ui.contact.view.ContactActivity

class ContactHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    val pic = itemView.findViewById<ImageView>(R.id.contactPic)
    val name = itemView.findViewById<TextView>(R.id.contactName)
    var number: String? = null

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (number == null)
            return
        val intent = Intent(view.context, ContactActivity::class.java)
        intent.putExtra("phoneNumber", number)
        view.context.startActivity(intent)
    }
}