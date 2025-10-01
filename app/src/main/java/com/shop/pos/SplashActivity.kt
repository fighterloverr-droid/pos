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

        // App ကို ပထမဆုံး run တဲ့အချိန်ကို မှတ်သားပါ
        LicenseManager.recordFirstRunTimestamp(this)

        Handler(Looper.getMainLooper()).postDelayed({
            // Workflow အသစ်ဖြင့် စစ်ဆေးပါ
            when {
                // 1. Activate ဖြစ်ပြီးသားလား?
                LicenseManager.isActivated(this) -> {
                    navigateToMainApp()
                }
                // 2. Trial ကာလ ကုန်သွားပြီလား?
                LicenseManager.isTrialExpired(this) -> {
                    startActivity(Intent(this, ActivationActivity::class.java))
                    finish()
                }
                // 3. Trial ကာလ မကုန်သေးရင် App ထဲဝင်ခွင့်ပြု
                else -> {
                    navigateToMainApp()
                }
            }
        }, 3000)
    }

    private fun navigateToMainApp() {
        if (PinManager.isPinEnabled(this)) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
        finish()
    }
}