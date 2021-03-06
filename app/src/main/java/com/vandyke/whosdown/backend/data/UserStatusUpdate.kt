/*
 * Copyright (c) 2017 Nicholas van Dyke
 * All rights reserved.
 */

package com.vandyke.whosdown.backend.data

import com.google.firebase.database.ServerValue

data class UserStatusUpdate(val down: Boolean = false,
                            val message: String = "") {
    val timestamp = ServerValue.TIMESTAMP
}