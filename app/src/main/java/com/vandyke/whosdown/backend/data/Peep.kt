/*
 * Copyright (c) 2017 Nicholas van Dyke
 * All rights reserved.
 */

package com.vandyke.whosdown.backend.data

data class Peep(val number: String,
                val name: String,
                val lookupKey: String,
                val photoUriString: String?,
                val down: Boolean,
                var message: String,
                val timestamp: Long)