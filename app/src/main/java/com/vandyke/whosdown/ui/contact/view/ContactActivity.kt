/*
 * Copyright (c) 2017 Nicholas van Dyke
 * All rights reserved.
 */

package com.vandyke.whosdown.ui.contact.view

import android.app.Activity
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.view.MenuItem
import android.widget.SeekBar
import com.vandyke.whosdown.R
import com.vandyke.whosdown.databinding.ActivityContactBinding
import com.vandyke.whosdown.ui.contact.viewmodel.ContactViewModel
import com.vandyke.whosdown.util.*
import kotlinx.android.synthetic.main.activity_contact.*

class ContactActivity : Activity() {

    var lookupKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var phoneNumber = intent.getStringExtra("phoneNumber")
        if (phoneNumber == null) {
            finish()
            return
        }
        phoneNumber = PhoneNumberUtils.formatNumberToE164(phoneNumber, getCountryCode())
        if (phoneNumber == null) {
            finish()
            return
        }

        val binding = DataBindingUtil.setContentView<ActivityContactBinding>(this, R.layout.activity_contact)
        val viewModel = ContactViewModel(application, phoneNumber)
        binding.viewModel = viewModel

        contactName.text = phoneNumber

        /* look up name and contact picture and lookup key*/
        val cursor = contentResolver.query(phoneNumber.toPhoneUri(),
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME_PRIMARY,
                        ContactsContract.PhoneLookup.PHOTO_URI,
                        ContactsContract.PhoneLookup.LOOKUP_KEY),
                null, null, null)
        /* set them */
        if (cursor != null && cursor.moveToFirst()) {
            contactName.text = cursor.getString(0)
            lookupKey = cursor.getString(2)
            val imageUriString = cursor.getString(1)
            if (imageUriString != null)
                contactPic.setImageURI(Uri.parse(imageUriString))
        }
        cursor?.close()

        contactText.setOnClickListener {
            startActivity(Intents.text(phoneNumber))
        }

        contactCall.setOnClickListener {
            startActivity(Intents.call(phoneNumber))
        }

        contactPic.setOnClickListener {
            if (lookupKey != null)
                startActivity(Intents.viewContact(lookupKey!!))
        }

        backButton.setOnClickListener {
            finish()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                when (progress) {
                    0 -> {
                        viewModel.subscribed.set(false)
                        viewModel.blocked.set(true)
                    }
                    1 -> {
                        viewModel.subscribed.set(false)
                        viewModel.blocked.set(false)
                    }
                    2 -> {
                        viewModel.subscribed.set(true)
                        viewModel.blocked.set(false)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        val defaultColors = followText.textColors

        viewModel.subscribed.observe { observable, i ->
            if (viewModel.subscribed.get()) {
                seekBar.progress = 2
                followText.setTextColor(getColorRes(R.color.colorAccent))
                blockText.setTextColor(defaultColors)
            } else if (!viewModel.blocked.get()) {
                seekBar.progress = 1
                followText.setTextColor(defaultColors)
                blockText.setTextColor(defaultColors)
            }
        }

        viewModel.blocked.observe { observable, i ->
            if (viewModel.blocked.get()) {
                seekBar.progress = 0
                blockText.setTextColor(getColorRes(R.color.colorAccent))
                followText.setTextColor(defaultColors)
            }
            else if (!viewModel.subscribed.get()) {
                seekBar.progress = 1
                followText.setTextColor(defaultColors)
                blockText.setTextColor(defaultColors)
            }
        }
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

    override fun onResume() {
        super.onResume()
        clearNotifications(this)
    }
}
