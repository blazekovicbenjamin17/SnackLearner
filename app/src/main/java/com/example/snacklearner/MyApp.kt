package com.example.snacklearner

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val darkModeEnabled = getSharedPreferences(
            "app_settings",
            Context.MODE_PRIVATE
        ).getBoolean("dark_mode_enabled", false)

        AppCompatDelegate.setDefaultNightMode(
            if (darkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
