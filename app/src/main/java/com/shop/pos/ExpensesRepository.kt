package com.shop.pos

object ExpensesRepository {
    private val expenseItems = mutableListOf<ExpenseItem>()

    init {
        loadSampleData()
    }

    private fun loadSampleData() {
        if (expenseItems.isEmpty()) {
            expenseItems.add(ExpenseItem("ဆိုင်ခန်းငှားခ (September)", "01-Sep-2025", 300000.0))
            expenseItems.add(ExpenseItem("အင်တာနက်ဘေလ်", "05-Sep-2025", 35000.0))
            expenseItems.add(ExpenseItem("ဝန်ထမ်းလစာ (August)", "30-Aug-2025", 500000.0))
        }
    }

    fun getExpenseItems(): List<ExpenseItem> {
        return expenseItems
    }

    // TODO: Add, Update, Delete functions will be added later
}