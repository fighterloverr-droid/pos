package com.shop.pos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SaleItem(
    val name: String,
    val quantity: Int,
    val price: Double, // ရောင်းဈေး
    val costPrice: Double, // အရင်းဈေး
    val imageUri: String? // <-- Field အသစ် (ပုံရဲ့ path ကို သိမ်းရန်)
) : Parcelable