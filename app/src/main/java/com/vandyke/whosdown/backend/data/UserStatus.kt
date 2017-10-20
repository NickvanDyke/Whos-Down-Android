/*
 * Copyright (c) 2017 Nicholas van Dyke
 * All rights reserved.
 */

package com.vandyke.whosdown.backend.data

data class UserStatus(val down: Boolean = false,
                      val message: String = "",
                      val timestamp: Long = 0)