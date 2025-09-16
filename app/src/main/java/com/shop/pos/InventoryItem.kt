package com.shop.pos

// Parcelable import တွေကို ထည့်ဖို့ မမေ့ပါနဲ့
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InventoryItem(
    val name: String,
    val stockQuantity: Int,
    val price: Double, // ရောင်းဈေး (Selling Price)
    val costPrice: Double, // အရင်းဈေး (Cost Price)
    val soldQuantity: Int = 0 // ရောင်းပြီးအရေအတွက် (default 0)
) : Parcelable