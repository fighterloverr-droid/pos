package com.shop.pos

object ExpensesRepository {
    private val expenseItems = mutableListOf<ExpenseItem>()

    fun getExpenseItems(): List<ExpenseItem> {
        return expenseItems
    }

    fun addExpenseItem(item: ExpenseItem) {
        expenseItems.add(0, item)
    }

    fun updateExpenseItem(position: Int, updatedItem: ExpenseItem) {
        if (position >= 0 && position < expenseItems.size) {
            expenseItems[position] = updatedItem
        }
    }

    fun deleteExpenseItem(position: Int) {
        if (position >= 0 && position < expenseItems.size) {
            expenseItems.removeAt(position)
        }
    }

    fun getTotalExpenses(): Double {
        return expenseItems.sumOf { it.amount }
    }
}