package com.shop.pos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PurchaseDao {
    @Insert
    suspend fun insert(purchase: PurchaseItem)

    @Update
    suspend fun update(purchase: PurchaseItem)

    @Query("SELECT * FROM purchase_records ORDER BY id DESC")
    suspend fun getAllPurchases(): List<PurchaseItem>
}