package com.shop.pos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExpensesDao {
    @Insert
    suspend fun insert(expenseItem: ExpenseItem)

    @Update
    suspend fun update(expenseItem: ExpenseItem)

    @Delete
    suspend fun delete(expenseItem: ExpenseItem)

    @Query("SELECT * FROM expense_records ORDER BY id DESC")
    suspend fun getAllExpenses(): List<ExpenseItem>

    @Query("SELECT SUM(amount) FROM expense_records")
    suspend fun getTotalExpenses(): Double?

    @Query("SELECT * FROM expense_records WHERE id = :id")
    suspend fun getExpenseById(id: Int): ExpenseItem?
}