package com.vandyke.whosdown.backend.data

data class Peep(val number: String,
                val name: String,
                val photoUriString: String?,
                val down: Boolean,
                var message: String,
                val timestamp: Long)