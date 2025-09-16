package com.shop.pos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PurchaseDetailItem(
    val name: String,
    val quantity: Int,
    val purchasePrice: Double // ဝယ်ဈေး
) : Parcelable