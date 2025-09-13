package com.shop.pos

data class PurchaseItem(
    val supplierName: String,
    val purchaseDate: String,
    val items: List<PurchaseDetailItem>, // ဝယ်ယူသည့် ပစ္စည်းများ list
    val totalAmount: Double // စုစုပေါင်းတန်ဖိုး
)