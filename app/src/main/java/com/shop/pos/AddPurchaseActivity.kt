package com.shop.pos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.DatePickerDialog
import android.widget.Button
import android.widget.EditText
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.widget.TextView
import android.widget.Toast

class AddPurchaseActivity : AppCompatActivity() {

    private lateinit var textViewPurchaseDate: TextView
    private lateinit var editTextSupplierName: EditText
    private lateinit var editTextTotalAmount: EditText
    private lateinit var buttonSavePurchase: Button
    private lateinit var toolbar: MaterialToolbar

    private val calendar = Calendar.getInstance()
    // ပြင်ဆင်မယ့် item ရဲ့ position ကို သိမ်းထားရန် (-1 ဆိုရင် item အသစ်)
    private var editingPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_purchase)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        textViewPurchaseDate = findViewById(R.id.textViewPurchaseDate)
        editTextSupplierName = findViewById(R.id.editTextSupplierName)
        editTextTotalAmount = findViewById(R.id.editTextTotalAmount)
        buttonSavePurchase = findViewById(R.id.buttonSavePurchase)

        // Intent ထဲမှာ ပြင်ဆင်ရန် position ပါလာသလား စစ်ဆေးပါ
        editingPosition = intent.getIntExtra("EDIT_PURCHASE_POSITION", -1)

        if (editingPosition != -1) {
            // Edit Mode
            toolbar.title = "အဝယ်မှတ်တမ်း ပြင်ဆင်ရန်"
            loadPurchaseData()
        } else {
            // Add Mode
            toolbar.title = "အဝယ်စာရင်းသစ်ထည့်ရန်"
            updateDateInView()
        }

        textViewPurchaseDate.setOnClickListener {
            showDatePickerDialog()
        }

        buttonSavePurchase.setOnClickListener {
            savePurchase()
        }
    }

    private fun loadPurchaseData() {
        val purchaseItem = PurchasesRepository.getPurchaseItems()[editingPosition]
        editTextSupplierName.setText(purchaseItem.supplierName)
        editTextTotalAmount.setText(purchaseItem.totalAmount.toString())
        textViewPurchaseDate.text = purchaseItem.purchaseDate

        // String ကနေ Date ပြန်ပြောင်းပြီး Calendar ကို set လုပ်ပါ
        val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        calendar.time = sdf.parse(purchaseItem.purchaseDate) ?: Date()
    }

    private fun showDatePickerDialog() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth)
                updateDateInView()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun updateDateInView() {
        val myFormat = "dd-MMM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        textViewPurchaseDate.text = sdf.format(calendar.time)
    }

    private fun savePurchase() {
        val supplierName = editTextSupplierName.text.toString()
        val totalAmountStr = editTextTotalAmount.text.toString()
        val purchaseDate = textViewPurchaseDate.text.toString()

        if (supplierName.isEmpty() || totalAmountStr.isEmpty()) {
            Toast.makeText(this, "ကျေးဇူးပြု၍ Supplier အမည်နှင့် တန်ဖိုးကို ဖြည့်ပါ", Toast.LENGTH_SHORT).show()
            return
        }

        val purchase = PurchaseItem(
            supplierName = supplierName,
            purchaseDate = purchaseDate,
            totalAmount = totalAmountStr.toDouble()
        )

        if (editingPosition != -1) {
            // Edit Mode မှာဆို update လုပ်ပါ
            PurchasesRepository.updatePurchaseItem(editingPosition, purchase)
            Toast.makeText(this, "ပြင်ဆင်ပြီးပါပြီ", Toast.LENGTH_SHORT).show()
        } else {
            // Add Mode မှာဆို အသစ်ထည့်ပါ
            PurchasesRepository.addPurchaseItem(purchase)
            Toast.makeText(this, "အောင်မြင်စွာ သိမ်းဆည်းပြီးပါပြီ", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}