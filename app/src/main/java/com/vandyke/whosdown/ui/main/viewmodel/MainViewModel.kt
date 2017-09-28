package com.vandyke.whosdown.ui.main.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.ObservableArrayList
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableList
import com.vandyke.whosdown.backend.data.Peep
import com.vandyke.whosdown.ui.main.model.FirebaseModel
import com.vandyke.whosdown.util.addOnPropertyChangedListener


class MainViewModel(application: Application) : AndroidViewModel(application) {
    val down: ObservableBoolean = ObservableBoolean(false)
    val message: ObservableField<String> = ObservableField("")
    val peeps: ObservableList<Peep> = ObservableArrayList<Peep>()

    val connected: ObservableBoolean = ObservableBoolean(false)

    private val model = FirebaseModel(this)

    init {
        model.setUserDbListener()
        model.setDbListeners(application)

        down.addOnPropertyChangedListener { sender, propertyId ->
            model.setUserDown((sender as ObservableBoolean).get())
        }

        message.addOnPropertyChangedListener { sender, propertyId ->
            model.setUserMessage((sender as ObservableField<String>).get())
        }
    }

    fun refreshListeners() {
        model.setDbListeners(getApplication())
    }

    fun updatePeeps(updatedPeep: Peep) {
        if (updatedPeep.down) {
            /* check if updatedPeep already exists in the list, and if it does, update it's message if necessary, then return */
            peeps.forEachIndexed { i, peep ->
                if (peep.number == updatedPeep.number) {
                    if (peep.message == updatedPeep.message) {
                        return
                    } else {
                        peeps[i] = updatedPeep /* need to set it to the updatedPeep, changing the existing one won't trigger the observable callback */
                        return
                    }
                }
            }
            /* if the updatedPeep isn't already in the list, it's added at the beginning */
            peeps.add(0, updatedPeep)
        } else { /* else the peep isn't down and will be removed from the list of down peeps */
            val index = peeps.indexOfFirst { it.number == updatedPeep.number }
            if (index != -1)
                peeps.removeAt(index) /* need to remove using removeAt, otherwise the observable callback method isn't called... */
        }
    }
}