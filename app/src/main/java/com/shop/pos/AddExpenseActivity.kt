package com.shop.pos

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var textViewExpenseDate: TextView
    private lateinit var editTextExpenseName: EditText
    private lateinit var editTextExpenseAmount: EditText
    private lateinit var buttonSaveExpense: Button
    private lateinit var toolbar: MaterialToolbar

    private val calendar = Calendar.getInstance()
    private var editingPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        textViewExpenseDate = findViewById(R.id.textViewExpenseDate)
        editTextExpenseName = findViewById(R.id.editTextExpenseName)
        editTextExpenseAmount = findViewById(R.id.editTextExpenseAmount)
        buttonSaveExpense = findViewById(R.id.buttonSaveExpense)

        editingPosition = intent.getIntExtra("EDIT_EXPENSE_POSITION", -1)

        if (editingPosition != -1) {
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
        val expenseItem = ExpensesRepository.getExpenseItems()[editingPosition]
        editTextExpenseName.setText(expenseItem.name)
        editTextExpenseAmount.setText(expenseItem.amount.toString())
        textViewExpenseDate.text = expenseItem.date

        val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        calendar.time = sdf.parse(expenseItem.date) ?: Date()
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

        if (name.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "ကျေးဇူးပြု၍ အမည်နှင့် ပမာဏကို ဖြည့်ပါ", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = ExpenseItem(
            name = name,
            date = date,
            amount = amountStr.toDouble()
        )

        if (editingPosition != -1) {
            ExpensesRepository.updateExpenseItem(editingPosition, expense)
            Toast.makeText(this, "ပြင်ဆင်ပြီးပါပြီ", Toast.LENGTH_SHORT).show()
        } else {
            ExpensesRepository.addExpenseItem(expense)
            Toast.makeText(this, "အောင်မြင်စွာ သိမ်းဆည်းပြီးပါပြီ", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}