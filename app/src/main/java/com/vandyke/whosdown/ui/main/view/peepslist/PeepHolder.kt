package com.vandyke.whosdown.ui.main.view.peepslist

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.vandyke.whosdown.R
import com.vandyke.whosdown.backend.data.Peep

class PeepHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    val pic = itemView.findViewById<ImageView>(R.id.peepPic)
    val name = itemView.findViewById<TextView>(R.id.peepName)
    val message = itemView.findViewById<TextView>(R.id.peepMessage)
    var number = ""

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(peep: Peep) {
        message.text = peep.message
        number = peep.number
        val cursor = name.context.contentResolver.query(
                Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(peep.number)),
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME,
                        ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI),
                null,
                null,
                null)
        if (!cursor.moveToFirst())
            return
        name.text = cursor.getString(0)
        val imageUriString = cursor.getString(1)
        if (imageUriString != null)
            pic.setImageURI(Uri.parse(imageUriString))
        else
            pic.setImageResource(R.drawable.person_placeholder)
        cursor.close()
    }

    override fun onClick(view: View) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("sms:" + number)
        view.context.startActivity(intent)
    }
}