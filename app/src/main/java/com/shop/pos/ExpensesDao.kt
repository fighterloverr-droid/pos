package com.shop.pos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExpensesDao {
    @Insert
    suspend fun insert(expenseItem: ExpenseItem)

    @Query("SELECT * FROM expense_records ORDER BY id DESC")
    suspend fun getAllExpenses(): List<ExpenseItem>

    @Query("SELECT SUM(amount) FROM expense_records")
    suspend fun getTotalExpenses(): Double?

    @Query("SELECT SUM(amount) FROM expense_records")
    suspend fun getTotalExpenses(): Double?
}