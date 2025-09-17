package com.shop.pos

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    // --- For Sale Items (from previous step) ---
    @TypeConverter
    fun fromSaleItemList(value: List<SaleItem>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toSaleItemList(value: String): List<SaleItem>? {
        val listType = object : TypeToken<List<SaleItem>>() {}.type
        return gson.fromJson(value, listType)
    }

    // --- For Purchase Items (Converter အသစ်) ---
    @TypeConverter
    fun fromPurchaseDetailItemList(value: List<PurchaseDetailItem>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toPurchaseDetailItemList(value: String): List<PurchaseDetailItem>? {
        val listType = object : TypeToken<List<PurchaseDetailItem>>() {}.type
        return gson.fromJson(value, listType)
    }
}