package com.vandyke.whosdown.ui.main.view.peepslist

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.provider.ContactsContract
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.vandyke.whosdown.R
import com.vandyke.whosdown.backend.data.Peep
import com.vandyke.whosdown.ui.contact.view.ContactActivity
import com.vandyke.whosdown.util.timePassed
import com.vandyke.whosdown.util.toPhoneUri
import com.vandyke.whosdown.util.toTimePassedString

class PeepHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    val pic = itemView.findViewById<ImageView>(R.id.peepPic)
    val name = itemView.findViewById<TextView>(R.id.peepName)
    val message = itemView.findViewById<TextView>(R.id.peepMessage)
    val time = itemView.findViewById<TextView>(R.id.peepTime)
    var number = ""

    val handler = Handler()

    init {
        itemView.setOnClickListener(this)
    }

    fun bind(peep: Peep) {
        handler.removeCallbacksAndMessages(null)
        message.text = peep.message
        time.text = peep.timestamp.toTimePassedString()
        number = peep.number
        val cursor = name.context.contentResolver.query(peep.number.toPhoneUri(),
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

        val runnable = object : Runnable {
            override fun run() {
                time.text = peep.timestamp.toTimePassedString()
                handler.postDelayed(this, 60000)
            }
        }

        val timeToMinute = 60 - (peep.timestamp.timePassed() / 1000 % 60)
        handler.postDelayed(runnable, timeToMinute * 1000)
    }

    override fun onClick(view: View) {
        val intent = Intent(view.context, ContactActivity::class.java)
        intent.putExtra("phoneNumber", number)
        view.context.startActivity(intent)
    }
}