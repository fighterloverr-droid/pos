package com.shop.pos

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

object LicenseManager {
    private const val PREFS_NAME = "license_prefs"
    private const val KEY_ACTIVATION_STATUS = "is_activated"
    private const val KEY_FIRST_RUN_TIMESTAMP = "first_run_timestamp"

    // --- Trial Duration ကို ဤနေရာတွင် ပုံသေ သတ်မှတ်ပါ ---
    const val TRIAL_DURATION_HOURS = 3L

    private const val SECRET_KEY = "My-Super-Secret-POS-App-2025-For-Everyone"

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun recordFirstRunTimestamp(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getLong(KEY_FIRST_RUN_TIMESTAMP, 0L) == 0L) {
            prefs.edit().putLong(KEY_FIRST_RUN_TIMESTAMP, System.currentTimeMillis()).apply()
        }
    }

    fun isTrialExpired(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val firstRunTimestamp = prefs.getLong(KEY_FIRST_RUN_TIMESTAMP, 0L)
        if (firstRunTimestamp == 0L) {
            return false
        }
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - firstRunTimestamp

        // ပုံသေကိန်းအသစ်ကို အသုံးပြုပါ
        return elapsedTime > TimeUnit.HOURS.toMillis(TRIAL_DURATION_HOURS)
    }

    // -------------------------------------------------------------------------
    // Activation Key Logic
    // -------------------------------------------------------------------------

    private fun generateKeyForDevice(context: Context): String {
        val deviceId = getDeviceId(context)
        val combinedString = "$deviceId-$SECRET_KEY"

        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combinedString.toByteArray(Charsets.UTF_8))

        val hexString = hashBytes.joinToString("") { "%02x".format(it) }
        return hexString.substring(0, 10).uppercase()
    }

    fun verifyActivationKey(context: Context, activationKeyFromUser: String): Boolean {
        return try {
            val cleanUserKey = activationKeyFromUser.replace("-", "").uppercase()
            val generatedKey = generateKeyForDevice(context)
            cleanUserKey == generatedKey
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // -------------------------------------------------------------------------
    // Activation State Save / Load
    // -------------------------------------------------------------------------

    fun isActivated(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ACTIVATION_STATUS, false)
    }

    fun saveActivationStatus(context: Context, isActivated: Boolean) {
        val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
        editor.putBoolean(KEY_ACTIVATION_STATUS, isActivated)
        editor.apply()
    }
}
