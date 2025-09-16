package com.shop.pos

// Parcelable import တွေကို ထည့်ဖို့ မမေ့ပါနဲ့
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class PurchaseItem(
    val supplierName: String,
    val purchaseDate: String,
    val items: @RawValue List<PurchaseDetailItem>, // Parcelable List အတွက် @RawValue ထည့်ပါ
    val totalAmount: Double,
    var hasArrived: Boolean = false // <-- Field အသစ် (var အဖြစ် ကြေညာရန် အရေးကြီးသည်)
) : Parcelable