/*
 * Copyright (c) 2017 Nicholas van Dyke
 * All rights reserved.
 */

package com.vandyke.whosdown.ui.main.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableList
import com.vandyke.whosdown.backend.data.Peep
import com.vandyke.whosdown.backend.data.UserStatusUpdate
import com.vandyke.whosdown.ui.main.model.MainModel
import com.vandyke.whosdown.util.observe


class MainViewModel(application: Application) : AndroidViewModel(application) {
    val down: ObservableBoolean = ObservableBoolean(false)
    val message: ObservableField<String> = ObservableField("")
    val peeps: ObservableList<Peep> = ObservableArrayList<Peep>()

    val connected: ObservableBoolean = ObservableBoolean(false)

    private val model = MainModel(this)

    /* set to true when the model is modifying down and message, and checked before modifying the model,
       so that changes from the model don't trigger more things. Maybe there's a better way to do this? */
    var modelMakingChanges = true

    init {
        model.setUserDbListener()
        model.setDbListeners(application)

        /* need to do this here because setting an onCheckedChangeListener for the down switch doesn't work for some reason, maybe due to data binding */
        down.observe { observable, i ->
            if (!modelMakingChanges)
                setUserStatus()
        }
    }

    fun setUserStatus() {
        model.setUserStatus(UserStatusUpdate(down.get(), message.get()))
    }

    fun refreshListeners() {
        model.setDbListeners(getApplication())
    }

    fun updatePeeps(updatedPeep: Peep) {
        /* remove regardless */
        val removeIndex = peeps.indexOfFirst { it.number == updatedPeep.number }
        if (removeIndex != -1)
            peeps.removeAt(removeIndex) /* need to remove using removeAt, otherwise the observable callback method isn't called... */

        /* if the updated peep is down, insert it at the appropriate position (based on timestamp) */
        if (updatedPeep.down) {
            var index = peeps.indexOfFirst { updatedPeep.timestamp > it.timestamp }
            if (index == -1) {
                index = if (peeps.isEmpty()) 0 else peeps.size
            }
            peeps.add(index, updatedPeep)
        }
    }

    fun removePeep(phoneNumber: String) {
        val index = peeps.indexOfFirst { it.number == phoneNumber }
        if (index != -1)
            peeps.removeAt(index)
    }
}