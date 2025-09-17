package com.shop.pos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PurchaseDao {
    @Insert
    suspend fun insert(purchase: PurchaseItem)

    @Update
    suspend fun update(purchase: PurchaseItem)

    @Delete
    suspend fun delete(purchase: PurchaseItem)

    @Query("SELECT * FROM purchase_records ORDER BY id DESC")
    suspend fun getAllPurchases(): List<PurchaseItem>

    @Query("SELECT SUM(totalAmount) FROM purchase_records")
    suspend fun getTotalPurchases(): Double?

    @Query("SELECT * FROM purchase_records WHERE id = :id")
    suspend fun getPurchaseById(id: Int): PurchaseItem?
}