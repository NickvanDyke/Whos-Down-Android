package com.vandyke.whosdown.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.databinding.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.vandyke.whosdown.auth
import com.vandyke.whosdown.data.DownStatus
import com.vandyke.whosdown.data.Peep
import com.vandyke.whosdown.database


class ViewModel(application: Application) : AndroidViewModel(application) {
    val down: ObservableBoolean = ObservableBoolean(false)
    val localUserMessage: ObservableField<String> = ObservableField("")
    val peeps: ObservableList<Peep> = ObservableArrayList<Peep>()
    val authorized: ObservableBoolean = ObservableBoolean(false)

    private val model = FirebaseModel(this)

    val localNumberListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val downStatus = dataSnapshot.getValue(DownStatus::class.java) ?: return
            println("new DownStatus for local user: $downStatus")
            down.set(downStatus.down)
            localUserMessage.set(downStatus.message)
        }

        override fun onCancelled(p0: DatabaseError?) {
            TODO("not implemented")
        }
    }

    init {
        authorized.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                if ((sender as ObservableBoolean).get()) {
                    database.child(auth.currentUser?.phoneNumber).addValueEventListener(localNumberListener)
                    model.setListeners(application)
                } else {
                    database.removeEventListener(localNumberListener)
                }
            }
        })

        authorized.set(auth.currentUser != null)

        localUserMessage.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                val msg = (sender as ObservableField<String>).get()
                if (authorized.get())
                    database.child(auth.currentUser?.phoneNumber).child("message").setValue(msg)
            }
        })
    }

    fun onDownSwitched(down: Boolean) {
        database.child(auth.currentUser?.phoneNumber).setValue(DownStatus(down, localUserMessage.get()))
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