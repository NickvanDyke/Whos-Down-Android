package com.vandyke.whosdown.ui.blocking

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.vandyke.whosdown.R

class BlockingHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    val pic = itemView.findViewById<ImageView>(R.id.contactPic)
    val name = itemView.findViewById<TextView>(R.id.contactName)
    var number = ""

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        // block the number
    }
}