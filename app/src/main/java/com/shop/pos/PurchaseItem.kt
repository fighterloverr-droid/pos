package com.shop.pos

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
@Entity(tableName = "purchase_records")
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val supplierName: String,
    val purchaseDate: String,
    val items: @RawValue List<PurchaseDetailItem>?, // <-- nullable ဖြစ်အောင် ? ထည့်ပါ
    val totalAmount: Double,
    val note: String?,
    var hasArrived: Boolean = false
) : Parcelable