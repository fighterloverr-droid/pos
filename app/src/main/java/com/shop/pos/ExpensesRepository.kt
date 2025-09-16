package com.shop.pos

// object အစား class အဖြစ်ပြောင်းပြီး dao ကို လက်ခံပါ
class ExpensesRepository(private val expensesDao: ExpensesDao) {

    suspend fun getExpenseItems(): List<ExpenseItem> {
        return expensesDao.getAllExpenses()
    }

    suspend fun addExpenseItem(item: ExpenseItem) {
        expensesDao.insert(item)
    }

    // TODO: Add Update and Delete functions later
}