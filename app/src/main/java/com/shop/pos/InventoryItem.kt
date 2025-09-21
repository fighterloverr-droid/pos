package com.shop.pos

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "inventory_items", indices = [Index(value = ["code"], unique = true)]) // Code ကို unique ဖြစ်အောင် index သတ်မှတ်ပါ
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val code: String, // <-- Field အသစ်
    val stockQuantity: Int,
    val price: Double, // ရောင်းဈေး
    val costPrice: Double, // အရင်းဈေး
    val soldQuantity: Int = 0,
    val isForSale: Boolean = true
) : Parcelable