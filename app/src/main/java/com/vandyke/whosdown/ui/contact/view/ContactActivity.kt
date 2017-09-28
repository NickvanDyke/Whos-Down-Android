package com.vandyke.whosdown.ui.contact.view

import android.app.Activity
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.MenuItem
import com.vandyke.whosdown.R
import com.vandyke.whosdown.databinding.ActivityContactBinding
import com.vandyke.whosdown.ui.contact.viewmodel.ContactViewModel

class ContactActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phoneNumber = intent.getStringExtra("phoneNumber")
        val name = intent.getStringExtra("name")
        if (phoneNumber == null || name == null) {
            finish()
            return
        }
        actionBar.title = name
        actionBar.setDisplayHomeAsUpEnabled(true)

        val binding = DataBindingUtil.setContentView<ActivityContactBinding>(this, R.layout.activity_contact)
        val viewModel = ContactViewModel(application, phoneNumber)
        binding.viewModel = viewModel
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> { finish(); return true }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
