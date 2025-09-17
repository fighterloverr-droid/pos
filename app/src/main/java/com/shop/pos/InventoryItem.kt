package com.shop.pos

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val stockQuantity: Int,
    val price: Double, // ရောင်းဈေး
    val costPrice: Double, // အရင်းဈေး
    val soldQuantity: Int = 0,
    val isForSale: Boolean = true // <-- Field အသစ် (default: true)
) : Parcelable