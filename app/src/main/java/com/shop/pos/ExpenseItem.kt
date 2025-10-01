package com.shop.pos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_records")
data class ExpenseItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val date: String,
    val amount: Double, // <-- ဒီနေရာမှာ comma (,) ကျန်နေခဲ့တာပါ
    val category: String
)