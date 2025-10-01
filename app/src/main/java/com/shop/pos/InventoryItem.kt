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
    val id: Long = 0, // Int အစား Long ကိုသုံးတာ ပိုကောင်းပါတယ်
    val name: String,
    val code: String?,
    var imageUri: String? = null,

    // Stock & Sales Fields
    val stockQuantity: Int,
    val soldQuantity: Int = 0, // << (၁) ခါသာ ထားရှိရန်

    // Pricing Fields
    val costPrice: Double,
    val price: Double,
    val wholesaleQuantity: Int? = null,
    val wholesalePrice: Double? = null,

    // Status
    val isForSale: Boolean = true

) : Parcelable