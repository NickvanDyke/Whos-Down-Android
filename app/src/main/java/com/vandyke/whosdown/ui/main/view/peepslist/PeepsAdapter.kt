package com.vandyke.whosdown.ui.main.view.peepslist

import android.databinding.ObservableList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.vandyke.whosdown.R
import com.vandyke.whosdown.data.Peep
import com.vandyke.whosdown.ui.main.viewmodel.MainViewModel

class PeepsAdapter(val viewModel: MainViewModel) : RecyclerView.Adapter<PeepHolder>() {
    init {
        viewModel.peeps.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableList<Peep>>() {
            override fun onChanged(sender: ObservableList<Peep>) {
                notifyDataSetChanged()
            }

            override fun onItemRangeChanged(sender: ObservableList<Peep>, positionStart: Int, itemCount: Int) {
                notifyItemRangeChanged(positionStart, itemCount)
            }

            override fun onItemRangeMoved(sender: ObservableList<Peep>, fromPosition: Int, toPosition: Int, itemCount: Int) {
                TODO("onItemRangeMoved not implemented")
            }

            override fun onItemRangeInserted(sender: ObservableList<Peep>, positionStart: Int, itemCount: Int) {
                notifyItemRangeInserted(positionStart, itemCount)
            }

            override fun onItemRangeRemoved(sender: ObservableList<Peep>, positionStart: Int, itemCount: Int) {
                notifyItemRangeRemoved(positionStart, itemCount)
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeepHolder {
        return PeepHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_peep, parent, false))
    }

    override fun onBindViewHolder(holder: PeepHolder, position: Int) {
        val peep = viewModel.peeps[position]
        holder.name.text = peep.name
        holder.message.text = peep.message
        holder.number = peep.number
//        holder.pic.setImageURI(peep.pic)
    }

    override fun getItemCount(): Int {
        return viewModel.peeps.size
    }
}