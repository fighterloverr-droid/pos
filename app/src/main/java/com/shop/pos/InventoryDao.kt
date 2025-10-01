package com.shop.pos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface InventoryDao {
    @Insert
    suspend fun insert(item: InventoryItem)

    @Update
    suspend fun update(item: InventoryItem)

    @Delete
    suspend fun delete(item: InventoryItem)

    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    suspend fun getAllItems(): List<InventoryItem>

    @Query("SELECT * FROM inventory_items WHERE name = :name LIMIT 1")
    suspend fun findItemByName(name: String): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE code = :code LIMIT 1")
    suspend fun findItemByCode(code: String): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE isForSale = 1 ORDER BY name ASC")
    suspend fun getItemsForSale(): List<InventoryItem>
}