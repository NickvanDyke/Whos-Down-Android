package com.vandyke.whosdown.ui.main.view.peepslist

import android.content.Context
import android.database.Cursor
import android.databinding.ObservableList
import android.provider.ContactsContract
import android.support.v7.widget.RecyclerView
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import com.vandyke.whosdown.R
import com.vandyke.whosdown.backend.data.Peep
import com.vandyke.whosdown.ui.main.viewmodel.MainViewModel
import com.vandyke.whosdown.util.getCountryCode

class PeepsAdapter(val viewModel: MainViewModel, context: Context) : RecyclerView.Adapter<PeepHolder>() {
    lateinit var cursor: Cursor
    val numbers = mutableListOf<String>()

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

        setupCursorAndNumbersList(context)
    }

    fun setupCursorAndNumbersList(context: Context) {
        cursor = context.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI),
                null, null, null)

        val numberCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val countryCode = context.getCountryCode()
        numbers.clear()

        for (i in 0 until cursor.count) {
            cursor.moveToPosition(i)
            numbers.add(PhoneNumberUtils.formatNumberToE164(cursor.getString(numberCol), countryCode) ?: "")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeepHolder {
        return PeepHolder(LayoutInflater.from(parent.context).inflate(R.layout.holder_peep, parent, false))
    }

    override fun onBindViewHolder(holder: PeepHolder, position: Int) {
        holder.bind(viewModel.peeps[position], cursor, numbers)
    }

    override fun getItemCount(): Int {
        return viewModel.peeps.size
    }
}