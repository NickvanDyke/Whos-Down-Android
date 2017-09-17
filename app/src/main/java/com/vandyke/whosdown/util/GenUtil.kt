package com.vandyke.whosdown.util

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.telephony.TelephonyManager



object GenUtil {
    fun getCountryCode(context: Context): String {
        return (context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager).networkCountryIso
    }
}