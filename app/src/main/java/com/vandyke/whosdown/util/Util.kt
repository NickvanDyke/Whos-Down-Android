package com.vandyke.whosdown.util

import android.app.NotificationManager
import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.Intent
import android.databinding.BaseObservable
import android.databinding.Observable
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.provider.ContactsContract
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager

fun clearNotifications(context: Context) {
    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
    PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet("notifications", mutableSetOf()).apply()
}

fun Context.getCountryCode(): String {
    return (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).networkCountryIso.toUpperCase()
}

fun String.toLocalizedE164(context: Context): String? {
    return PhoneNumberUtils.formatNumberToE164(this, context.getCountryCode())
}

fun String.toPhoneUri(): Uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(this))

fun Long.toTimePassedString(): String {
    val difference = System.currentTimeMillis() - this
    return when {
        difference > 86400000 -> (difference / 86400000).toString() + "d"
        difference > 3600000 -> (difference / 3600000).toString() + "h"
        difference > 60000 -> (difference / 60000).toString() + "m"
        else -> "just now"
    }
}

fun Long.timePassed(): Long = System.currentTimeMillis() - this

fun BaseObservable.addOnPropertyChangedListener(function: (Observable, Int) -> Unit) {
    this.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
            function(sender, propertyId)
        }
    })
}

fun textIntent(number: String): Intent {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse("sms:" + number)
    return intent
}

fun callIntent(number: String) = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null))

fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
    var drawable = ContextCompat.getDrawable(context, drawableId)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        drawable = DrawableCompat.wrap(drawable).mutate()
    }

    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}