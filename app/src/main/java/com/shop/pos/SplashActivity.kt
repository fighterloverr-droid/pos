package com.shop.pos

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 3 စက္ကန့် (3000 milliseconds) ကြာပြီးနောက်မှာ နောက် screen ကို ကူးပြောင်းပါ
        Handler(Looper.getMainLooper()).postDelayed({
            // SharedPreferences ထဲမှာ PIN Lock ဖွင့်ထား/မထား စစ်ဆေးပါ
            if (PinManager.isPinEnabled(this)) {
                // ဖွင့်ထားရင် PIN Screen (MainActivity) ကို သွားပါ
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // ပိတ်ထားရင် Dashboard ကို တိုက်ရိုက်သွားပါ
                startActivity(Intent(this, DashboardActivity::class.java))
            }
            // လက်ရှိ SplashActivity ကို ပိတ်ပါ
            finish()
        }, 3000) // 3000ms = 3 seconds
    }
}