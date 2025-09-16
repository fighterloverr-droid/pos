package com.shop.pos

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
@Entity(tableName = "purchase_records") // <-- Entity အဖြစ် သတ်မှတ်ပါ
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true) // <-- Primary Key (ID) အဖြစ် သတ်မှတ်ပါ
    val id: Int = 0,
    val supplierName: String,
    val purchaseDate: String,
    val items: @RawValue List<PurchaseDetailItem>,
    val totalAmount: Double,
    var hasArrived: Boolean = false
) : Parcelable