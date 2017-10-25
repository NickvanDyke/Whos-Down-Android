/*
 * Copyright (c) 2017 Nicholas van Dyke
 * All rights reserved.
 */

package com.vandyke.whosdown.ui.contact.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import com.vandyke.whosdown.ui.contact.model.ContactModel
import com.vandyke.whosdown.util.observe

class ContactViewModel(application: Application, phoneNumber: String) : AndroidViewModel(application) {
    val down = ObservableBoolean(false)
    val message = ObservableField<String>()
    val subscribed = ObservableBoolean(false)
    val blocked = ObservableBoolean(false)
    val connected = ObservableBoolean(false)

    private val model = ContactModel(this, phoneNumber)

    init {
        subscribed.observe { observable, i ->
            model.setSubscribed((observable as ObservableBoolean).get())
        }

        blocked.observe { observable, i ->
            model.setBlocked((observable as ObservableBoolean).get())
        }
    }
}