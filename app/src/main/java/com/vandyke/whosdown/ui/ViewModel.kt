package com.vandyke.whosdown.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.*
import com.vandyke.whosdown.data.Peep


class ViewModel(application: Application) : AndroidViewModel(application) {
    val down: ObservableBoolean = ObservableBoolean(false)
    val message: ObservableField<String> = ObservableField("")
    val peeps: ObservableList<Peep> = ObservableArrayList<Peep>()

    val connected: ObservableBoolean = ObservableBoolean(false)

    private val model = FirebaseModel(this)

    init {
        model.setListeners(application)

        message.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                val msg = (sender as ObservableField<String>).get()
                model.setUserMessage(msg)
            }
        })
    }

    fun onDownSwitched(down: Boolean) {
        model.setUserDown(down)
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