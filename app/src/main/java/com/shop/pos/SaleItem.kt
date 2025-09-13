package com.shop.pos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize // <-- Annotation အသစ်ထည့်ပါ
data class SaleItem(
    val name: String,
    val quantity: Int,
    val price: Double
) : Parcelable // <-- Interface အသစ်ထည့်ပါ