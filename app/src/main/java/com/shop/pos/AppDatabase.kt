package com.shop.pos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// 1. Customer::class ကို entities ထဲမှာ ထပ်ထည့်ပါ
// 2. version ကို 7 သို့ တိုးမြှင့်ပါ
@Database(entities = [InventoryItem::class, PurchaseItem::class, SaleRecord::class, ExpenseItem::class, Customer::class], version = 8, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun inventoryDao(): InventoryDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun salesDao(): SalesDao
    abstract fun expensesDao(): ExpensesDao
    abstract fun customerDao(): CustomerDao // <-- DAO အသစ်

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pos_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}