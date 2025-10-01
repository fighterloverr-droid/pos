package com.shop.pos

import android.app.Application

class PosApplication : Application() {
    // App တစ်ခုလုံးအတွက် database instance ကို ဒီနေရာမှာ တစ်ခါတည်း တည်ဆောက်ထားပါ
    // This creates a single database instance for the entire application.
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}

