package com.shop.pos

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // xml ထဲက Settings button ကို ရှာပါ
        val buttonSettings = findViewById<ImageButton>(R.id.buttonSettings)

        // Button ကို နှိပ်လိုက်ရင် SettingsActivity ကို ဖွင့်ပေးဖို့ listener တပ်ပါ
        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }
}