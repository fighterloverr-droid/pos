package com.shop.pos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SalesDao {
    @Insert
    suspend fun insert(saleRecord: SaleRecord)

    @Update // <-- Function အသစ်
    suspend fun update(saleRecord: SaleRecord)

    @Delete
    suspend fun delete(saleRecord: SaleRecord)

    @Query("SELECT * FROM sales_records ORDER BY id DESC")
    suspend fun getAllSales(): List<SaleRecord>

    @Query("SELECT SUM(totalAmount) FROM sales_records")
    suspend fun getTotalSales(): Double?

    @Query("SELECT * FROM sales_records WHERE id = :id")
    suspend fun getSaleById(id: Int): SaleRecord?

    @Query("SELECT * FROM sales_records WHERE customerName = :customerName ORDER BY id DESC")
    suspend fun getSalesForCustomer(customerName: String): List<SaleRecord>

}