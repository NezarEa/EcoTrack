package com.ecotrack.Data

import java.util.prefs.Preferences

data class User(
    val id: String,
    val name: String,
    val email: String,
    val preferences: Preferences
)

