package com.shop.pos

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "inventory_items", indices = [Index(value = ["code"], unique = true)])
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val code: String?,
    var imageUri: String? = null, // <-- Field အသစ် (ပုံရဲ့ path ကို သိမ်းရန်)
    val stockQuantity: Int,
    val price: Double,
    val costPrice: Double,
    val soldQuantity: Int = 0,
    val isForSale: Boolean = true
) : Parcelable
