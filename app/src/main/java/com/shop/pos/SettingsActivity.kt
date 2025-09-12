package com.shop.pos

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchPinLock: SwitchMaterial
    private lateinit var layoutChangePin: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // UI element တွေကို ချိတ်ဆက်ပါ
        switchPinLock = findViewById(R.id.switchPinLock)
        layoutChangePin = findViewById(R.id.layoutChangePin)

        // လက်ရှိ settings အခြေအနေကို UI မှာ တင်ပြပါ
        loadSettings()

        // Switch ကို နှိပ်လိုက်ရင် အခြေအနေကို သိမ်းဆည်းပါ
        switchPinLock.setOnCheckedChangeListener { _, isChecked ->
            PinManager.setPinEnabled(this, isChecked)
            val status = if (isChecked) "ဖွင့်လိုက်ပါပြီ" else "ပိတ်လိုက်ပါပြီ"
            Toast.makeText(this, "PIN Lock ကို $status", Toast.LENGTH_SHORT).show()
        }

        // "PIN ပြောင်းရန်" ကို နှိပ်လိုက်ရင် dialog box ပြပါ
        layoutChangePin.setOnClickListener {
            showChangePinDialog()
        }
    }

    // သိမ်းဆည်းထားတဲ့ settings တွေကို UI မှာ ပြန်ဖော်ပြပေးတဲ့ function
    private fun loadSettings() {
        switchPinLock.isChecked = PinManager.isPinEnabled(this)
    }

    // PIN အသစ်ထည့်သွင်းဖို့ Dialog Box ပြပေးမယ့် function
    private fun showChangePinDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("PIN နံပါတ် အသစ် ထည့်သွင်းပါ")

        // PIN ရိုက်ထည့်ဖို့ EditText တစ်ခု ဖန်တီးပါ
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.hint = "ဂဏန်း ၄ လုံး ထည့်ပါ"
        builder.setView(input)

        // "သိမ်းမည်" Button
        builder.setPositiveButton("သိမ်းမည်") { dialog, _ ->
            val newPin = input.text.toString()
            if (newPin.length == 4 && newPin.all { it.isDigit() }) {
                PinManager.savePin(this, newPin)
                Toast.makeText(this, "PIN နံပါတ်အသစ် သိမ်းဆည်းပြီးပါပြီ", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "ကျေးဇူးပြု၍ ဂဏန်း ၄ လုံး ထည့်ပါ", Toast.LENGTH_SHORT).show()
            }
        }

        // "မလုပ်တော့ပါ" Button
        builder.setNegativeButton("မလုပ်တော့ပါ") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}