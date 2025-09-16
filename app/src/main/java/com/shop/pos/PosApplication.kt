package com.shop.pos

import android.app.Application

class PosApplication : Application() {
    // Database instance ကို App တစ်ခုလုံးအတွက် တစ်ခါပဲ တည်ဆောက်ပါမယ်
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}