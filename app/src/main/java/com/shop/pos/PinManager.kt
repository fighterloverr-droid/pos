package com.shop.pos

import android.content.Context
import android.content.SharedPreferences

object PinManager {

    private const val PREFS_NAME = "pos_prefs"
    private const val KEY_PIN = "saved_pin"
    private const val KEY_PIN_ENABLED = "is_pin_enabled"

    // SharedPreferences ကို ရယူရန် helper function
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // PIN နံပါတ်ကို သိမ်းဆည်းရန်
    fun savePin(context: Context, pin: String) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_PIN, pin)
        editor.apply()
    }

    // သိမ်းဆည်းထားသော PIN ကို ပြန်လည်ရယူရန်
    fun getPin(context: Context): String? {
        // မူရင်း PIN အဖြစ် "1234" ကို သတ်မှတ်ထားပေးသည်
        return getPreferences(context).getString(KEY_PIN, "1234")
    }

    // PIN Lock ဖွင့်/ပိတ် အခြေအနေကို သိမ်းဆည်းရန်
    fun setPinEnabled(context: Context, isEnabled: Boolean) {
        val editor = getPreferences(context).edit()
        editor.putBoolean(KEY_PIN_ENABLED, isEnabled)
        editor.apply()
    }

    // PIN Lock ဖွင့်/ပိတ် အခြေအနေကို ပြန်လည်ရယူရန်
    fun isPinEnabled(context: Context): Boolean {
        // default value ကို true အစား false သို့ ပြောင်းပါ
        return getPreferences(context).getBoolean(KEY_PIN_ENABLED, false)
    }
}