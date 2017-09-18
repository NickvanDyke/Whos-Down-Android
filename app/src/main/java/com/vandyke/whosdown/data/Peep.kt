package com.vandyke.whosdown.data

import android.net.Uri

data class Peep(val name: String,
                val pic: Uri,
                val number: String,
                val down: Boolean,
                var message: String)