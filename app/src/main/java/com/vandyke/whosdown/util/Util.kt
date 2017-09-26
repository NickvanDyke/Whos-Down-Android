package com.vandyke.whosdown.util

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.databinding.BaseObservable
import android.databinding.Observable
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager

fun Context.getCountryCode(): String {
    return (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).networkCountryIso.toUpperCase()
}

fun String.toLocalizedE164(context: Context): String? {
    return PhoneNumberUtils.formatNumberToE164(this, context.getCountryCode())
}

fun BaseObservable.addOnPropertyChangedListener(function: (Observable, Int) -> Unit) {
    this.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
            function(sender, propertyId)
        }
    })
}