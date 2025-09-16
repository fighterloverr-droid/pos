package com.shop.pos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [InventoryItem::class, PurchaseItem::class], version = 1, exportSchema = false) // PurchaseItem ကိုထည့်ပါ
@TypeConverters(Converters::class) // Converters ကိုထည့်ပါ
abstract class AppDatabase : RoomDatabase() {

    abstract fun inventoryDao(): InventoryDao
    abstract fun purchaseDao(): PurchaseDao // PurchaseDao ကိုထည့်ပါ

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pos_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}