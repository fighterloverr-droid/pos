package com.shop.pos

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ActivationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)

        val textViewDeviceId = findViewById<TextView>(R.id.textViewDeviceId)
        val buttonCopyId = findViewById<Button>(R.id.buttonCopyId)
        val editTextActivationKey = findViewById<EditText>(R.id.editTextActivationKey)
        val buttonActivate = findViewById<Button>(R.id.buttonActivate)

        // LicenseManager ကိုသုံးပြီး Device ID ကို ရယူပြသပါ
        val deviceId = LicenseManager.getDeviceId(this)
        textViewDeviceId.text = deviceId

        // "Copy ID" ခလုတ်ကို နှိပ်ရင် အလုပ်လုပ်မယ့် logic
        buttonCopyId.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Device ID", deviceId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Device ID ကို Copy ကူးပြီးပါပြီ!", Toast.LENGTH_SHORT).show()
        }

        // "Activate" ခလုတ်ကို နှိပ်ရင် အလုပ်လုပ်မယ့် logic
        buttonActivate.setOnClickListener {
            val key = editTextActivationKey.text.toString().trim()
            if (key.isEmpty()) {
                Toast.makeText(this, "ကျေးဇူးပြု၍ activation key ကို ထည့်ပါ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // LicenseManager ကိုသုံးပြီး Key ကို စစ်ဆေးပါ
            val isValid = LicenseManager.verifyActivationKey(this, key)
            if (isValid) {
                // Key မှန်ရင် status ကို save ပြီး App ကို restart လုပ်ပါ
                LicenseManager.saveActivationStatus(this, true)
                Toast.makeText(this, "Activation အောင်မြင်ပါသည်!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, SplashActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(this, "Activation Key မှားယွင်းနေပါသည်", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

