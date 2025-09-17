package com.shop.pos

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
@Entity(tableName = "sales_records")
data class SaleRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val items: @RawValue List<SaleItem>,
    val subtotal: Double,
    val discount: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val paymentType: String,
    var paymentStatus: String = paymentType,
    val isDelivered: Boolean = false, // <-- Field အသစ်
    val saleDate: String
) : Parcelable