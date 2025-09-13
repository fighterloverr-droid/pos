package com.shop.pos

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize // <-- Annotation အသစ်ထည့်ပါ
data class SaleRecord(
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val items: List<SaleItem>,
    val subtotal: Double,
    val discount: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val paymentType: String,
    val saleDate: String
) : Parcelable // <-- Interface အသစ်ထည့်ပါ