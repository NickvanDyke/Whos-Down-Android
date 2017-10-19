package com.vandyke.whosdown.ui.main.view.peepslist

import android.net.Uri
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.vandyke.whosdown.R
import com.vandyke.whosdown.backend.data.Peep
import com.vandyke.whosdown.util.Intents
import com.vandyke.whosdown.util.timePassed
import com.vandyke.whosdown.util.toTimePassedString

class PeepHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val pic = itemView.findViewById<ImageView>(R.id.peepPic)
    private val name = itemView.findViewById<TextView>(R.id.peepName)
    private val message = itemView.findViewById<TextView>(R.id.peepMessage)
    private val time = itemView.findViewById<TextView>(R.id.peepTime)

    var phoneNumber = ""
    var lookupKey = ""
    val handler = Handler()

    init {
        itemView.setOnClickListener {
            itemView.context.startActivity(Intents.contactActivity(itemView.context, phoneNumber))
        }

        pic.setOnClickListener {
            itemView.context.startActivity(Intents.viewContact(lookupKey))
        }
    }

    fun bind(peep: Peep) {
        handler.removeCallbacksAndMessages(null)
        name.text = peep.name
        message.text = peep.message
        time.text = peep.timestamp.toTimePassedString()
        phoneNumber = peep.number
        lookupKey = peep.lookupKey
        if (peep.photoUriString != null)
            pic.setImageURI(Uri.parse(peep.photoUriString))
        else
            pic.setImageResource(R.drawable.person_placeholder)

        val runnable = object : Runnable {
            override fun run() {
                time.text = peep.timestamp.toTimePassedString()
                handler.postDelayed(this, 60000)
            }
        }
        val timeToMinute = 60 - (peep.timestamp.timePassed() / 1000 % 60)
        handler.postDelayed(runnable, timeToMinute * 1000)
    }
}