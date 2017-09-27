package com.vandyke.whosdown.ui.contact.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableBoolean
import com.vandyke.whosdown.ui.contact.model.ContactModel
import com.vandyke.whosdown.util.addOnPropertyChangedListener

class ContactViewModel(application: Application, phoneNumber: String) : AndroidViewModel(application) {
    val subscribed = ObservableBoolean(false)
    val blocked = ObservableBoolean(false)

    val model = ContactModel(this, phoneNumber)

    init {
        subscribed.addOnPropertyChangedListener { observable, i ->
            model.setSubscribed((observable as ObservableBoolean).get())
        }

        blocked.addOnPropertyChangedListener { observable, i ->
            model.setBlocked((observable as ObservableBoolean).get())
        }
    }
}