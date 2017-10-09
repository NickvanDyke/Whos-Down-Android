package com.vandyke.whosdown.ui.contact.view

import android.app.Activity
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.MenuItem
import com.vandyke.whosdown.R
import com.vandyke.whosdown.databinding.ActivityContactBinding
import com.vandyke.whosdown.ui.contact.viewmodel.ContactViewModel
import com.vandyke.whosdown.util.phoneNumberUri
import kotlinx.android.synthetic.main.activity_contact.*

class ContactActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phoneNumber = intent.getStringExtra("phoneNumber")
        if (phoneNumber == null) {
            finish()
            return
        }
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.title = phoneNumber

        val binding = DataBindingUtil.setContentView<ActivityContactBinding>(this, R.layout.activity_contact)
        val viewModel = ContactViewModel(application, phoneNumber)
        binding.viewModel = viewModel

        /* look up name and contact picture */
        val cursor = contentResolver.query(phoneNumberUri(phoneNumber),
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_URI),
                null, null, null)
        /* set them */
        if (cursor.moveToFirst()) {
            actionBar.title = cursor.getString(0)
            val imageUriString = cursor.getString(1)
            if (imageUriString != null)
                contactPic.setImageURI(Uri.parse(imageUriString))
        }
        cursor.close()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
