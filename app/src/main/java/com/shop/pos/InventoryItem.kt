package com.shop.pos

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "inventory_items") // <-- Entity အဖြစ် သတ်မှတ်ပါ
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) // <-- Primary Key (ID) အဖြစ် သတ်မှတ်ပါ
    val id: Int = 0,
    val name: String,
    val stockQuantity: Int,
    val price: Double,
    val costPrice: Double,
    val soldQuantity: Int = 0
) : Parcelable