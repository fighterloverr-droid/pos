package com.shop.pos

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromPurchaseDetailItemList(value: List<PurchaseDetailItem>): String {
        val gson = Gson()
        val type = object : TypeToken<List<PurchaseDetailItem>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toPurchaseDetailItemList(value: String): List<PurchaseDetailItem> {
        val gson = Gson()
        val type = object : TypeToken<List<PurchaseDetailItem>>() {}.type
        return gson.fromJson(value, type)
    }
}