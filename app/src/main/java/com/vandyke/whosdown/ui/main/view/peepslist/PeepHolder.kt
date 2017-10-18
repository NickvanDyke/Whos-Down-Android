package com.vandyke.whosdown.ui.main.view.peepslist

import android.content.Intent
import android.database.Cursor
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

    fun bind(peep: Peep, cursor: Cursor, numbers: MutableList<String>) {
        handler.removeCallbacksAndMessages(null)
        message.text = peep.message
        time.text = peep.timestamp.toTimePassedString()
        number = peep.number

        val runnable = object : Runnable {
            override fun run() {
                time.text = peep.timestamp.toTimePassedString()
                handler.postDelayed(this, 60000)
            }
        }
        val timeToMinute = 60 - (peep.timestamp.timePassed() / 1000 % 60)
        handler.postDelayed(runnable, timeToMinute * 1000)

        val nameCol = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
        val uriCol = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)

        val index = numbers.indexOfFirst { it == number }
        if (index == -1) {
            name.text = "null"
            pic.setImageResource(R.drawable.person_placeholder)
            return
        }

        cursor.moveToPosition(index)
        name.text = cursor.getString(nameCol)
        val imageUriString = cursor.getString(uriCol)
        if (imageUriString != null)
            pic.setImageURI(Uri.parse(imageUriString))
        else
            pic.setImageResource(R.drawable.person_placeholder)
    }

    override fun onClick(view: View) {
        val intent = Intent(view.context, ContactActivity::class.java)
        intent.putExtra("phoneNumber", number)
        view.context.startActivity(intent)
    }
}