package com.vandyke.whosdown.ui.blocking

import android.app.Activity
import android.content.CursorLoader
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import com.vandyke.whosdown.R
import kotlinx.android.synthetic.main.activity_blocking.*

class BlockingActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocking)

        actionBar.title = "Block contacts"

        blockingList.addItemDecoration(DividerItemDecoration(blockingList.context, (blockingList.layoutManager as LinearLayoutManager).orientation))

        val cursorLoader = CursorLoader(this,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.Contacts.PHOTO_THUMBNAIL_URI),
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)

        blockingList.adapter = BlockingAdapter(cursorLoader.loadInBackground())
    }
}
