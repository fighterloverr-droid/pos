package com.shop.pos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SalesDao {
    @Insert
    suspend fun insert(saleRecord: SaleRecord)

    @Query("SELECT * FROM sales_records ORDER BY id DESC")
    suspend fun getAllSales(): List<SaleRecord>

    @Query("SELECT SUM(totalAmount) FROM sales_records")
    suspend fun getTotalSales(): Double?
}


