package com.gorkemoji.meteo.utils

import android.content.Context
import androidx.core.content.edit

object PreferencesHelper {
    private const val PREF_NAME = "preferences"

    fun save(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit() { putString(key, value) }
    }

    fun get(context: Context, key: String): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(key, null)
    }
}