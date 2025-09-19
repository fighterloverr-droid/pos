package com.shop.pos

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var textViewExpenseDate: TextView
    private lateinit var editTextExpenseName: EditText
    private lateinit var editTextExpenseAmount: EditText
    private lateinit var editTextExpenseCategory: EditText // <-- UI element အသစ်
    private lateinit var buttonSaveExpense: Button
    private lateinit var toolbar: MaterialToolbar
    private lateinit var expensesRepository: ExpensesRepository

    private val calendar = Calendar.getInstance()
    private var editingExpenseId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val dao = (application as PosApplication).database.expensesDao()
        expensesRepository = ExpensesRepository(dao)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        textViewExpenseDate = findViewById(R.id.textViewExpenseDate)
        editTextExpenseName = findViewById(R.id.editTextExpenseName)
        editTextExpenseAmount = findViewById(R.id.editTextExpenseAmount)
        editTextExpenseCategory = findViewById(R.id.editTextExpenseCategory) // <-- ချိတ်ဆက်ပါ
        buttonSaveExpense = findViewById(R.id.buttonSaveExpense)

        editingExpenseId = intent.getIntExtra("EDIT_EXPENSE_ID", -1)

        if (editingExpenseId != -1) {
            toolbar.title = "ကုန်ကျစရိတ် ပြင်ဆင်ရန်"
            loadExpenseData()
        } else {
            toolbar.title = "ကုန်ကျစရိတ်အသစ်ထည့်ရန်"
            updateDateInView()
        }

        textViewExpenseDate.setOnClickListener {
            showDatePickerDialog()
        }

        buttonSaveExpense.setOnClickListener {
            saveExpense()
        }
    }

    private fun loadExpenseData() {
        lifecycleScope.launch {
            val expenseItem = expensesRepository.getExpenseById(editingExpenseId)
            if (expenseItem != null) {
                editTextExpenseName.setText(expenseItem.name)
                editTextExpenseAmount.setText(expenseItem.amount.toString())
                editTextExpenseCategory.setText(expenseItem.category) // <-- data load လုပ်ပါ
                textViewExpenseDate.text = expenseItem.date

                val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                calendar.time = sdf.parse(expenseItem.date) ?: Date()
            }
        }
    }

    private fun showDatePickerDialog() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            calendar.set(Calendar.YEAR, selectedYear)
            calendar.set(Calendar.MONTH, selectedMonth)
            calendar.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)
            updateDateInView()
        }, year, month, day).show()
    }

    private fun updateDateInView() {
        val myFormat = "dd-MMM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        textViewExpenseDate.text = sdf.format(calendar.time)
    }

    private fun saveExpense() {
        val name = editTextExpenseName.text.toString()
        val amountStr = editTextExpenseAmount.text.toString()
        val date = textViewExpenseDate.text.toString()
        val category = editTextExpenseCategory.text.toString()

        if (name.isEmpty() || amountStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "အကွက်အားလုံးကို ဖြည့်ပါ", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            if (editingExpenseId != -1) {
                val updatedExpense = ExpenseItem(
                    id = editingExpenseId,
                    name = name,
                    date = date,
                    amount = amountStr.toDouble(),
                    category = category
                )
                expensesRepository.updateExpenseItem(updatedExpense)
                Toast.makeText(this@AddExpenseActivity, "ပြင်ဆင်ပြီးပါပြီ", Toast.LENGTH_SHORT).show()
            } else {
                val newExpense = ExpenseItem(
                    name = name,
                    date = date,
                    amount = amountStr.toDouble(),
                    category = category
                )
                expensesRepository.addExpenseItem(newExpense)
                Toast.makeText(this@AddExpenseActivity, "အောင်မြင်စွာ သိမ်းဆည်းပြီးပါပြီ", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}