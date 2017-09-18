package com.vandyke.whosdown.ui.main.view.peepslist

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.vandyke.whosdown.R

class PeepHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    val pic = itemView.findViewById<ImageView>(R.id.peepPic)
    val name = itemView.findViewById<TextView>(R.id.peepName)
    val message = itemView.findViewById<TextView>(R.id.peepMessage)
    var number = ""

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("sms:" + number)
        view.context.startActivity(intent)
    }
}