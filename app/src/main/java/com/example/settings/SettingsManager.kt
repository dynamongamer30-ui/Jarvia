package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SettingsManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "jarvis_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var geminiApiKey: String
        get() = sharedPrefs.getString("gemini_api_key", "") ?: ""
        set(value) = sharedPrefs.edit().putString("gemini_api_key", value).apply()

    var rivaApiKey: String
        get() = sharedPrefs.getString("riva_api_key", "") ?: ""
        set(value) = sharedPrefs.edit().putString("riva_api_key", value).apply()

    var ttsPitch: Float
        get() = sharedPrefs.getFloat("tts_pitch", 1.0f)
        set(value) = sharedPrefs.edit().putFloat("tts_pitch", value).apply()

    var ttsSpeed: Float
        get() = sharedPrefs.getFloat("tts_speed", 1.0f)
        set(value) = sharedPrefs.edit().putFloat("tts_speed", value).apply()

    var wakeWordSensitivity: Float
        get() = sharedPrefs.getFloat("wake_word_sensitivity", 0.5f)
        set(value) = sharedPrefs.edit().putFloat("wake_word_sensitivity", value).apply()

    var onDeviceOnly: Boolean
        get() = sharedPrefs.getBoolean("on_device_only", false)
        set(value) = sharedPrefs.edit().putBoolean("on_device_only", value).apply()
}
