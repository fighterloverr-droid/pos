package com.shop.pos

class ExpensesRepository(private val expensesDao: ExpensesDao) {

    suspend fun getExpenseItems(): List<ExpenseItem> {
        return expensesDao.getAllExpenses()
    }

    suspend fun addExpenseItem(item: ExpenseItem) {
        expensesDao.insert(item)
    }

    suspend fun updateExpenseItem(item: ExpenseItem) {
        expensesDao.update(item)
    }

    suspend fun deleteExpenseItem(item: ExpenseItem) {
        expensesDao.delete(item)
    }

    suspend fun getExpenseById(id: Int): ExpenseItem? {
        return expensesDao.getExpenseById(id)
    }

    suspend fun getTotalExpenses(): Double? {
        return expensesDao.getTotalExpenses()
    }
}