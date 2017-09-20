package com.vandyke.whosdown.util

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager

fun Context.getCountryCode(): String {
    return (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).networkCountryIso.toUpperCase()
}

fun String.toLocalizedE164(context: Context): String? {
    return PhoneNumberUtils.formatNumberToE164(this, context.getCountryCode())
}