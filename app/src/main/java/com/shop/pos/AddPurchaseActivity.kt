package com.shop.pos

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddPurchaseActivity : AppCompatActivity(), PurchaseDetailItemListener {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var editTextSupplierName: EditText
    private lateinit var textViewPurchaseDate: TextView
    private lateinit var recyclerViewPurchaseItems: RecyclerView
    private lateinit var buttonAddItemToPurchase: Button
    private lateinit var textViewTotalAmount: TextView
    private lateinit var buttonSavePurchase: Button
    private lateinit var editTextPurchaseNote: EditText

    private lateinit var purchaseDetailAdapter: PurchaseDetailAdapter
    private val purchaseDetailItems = mutableListOf<PurchaseDetailItem>()

    private lateinit var purchasesRepository: PurchasesRepository

    private val calendar = Calendar.getInstance()
    private var editingPurchaseId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_purchase)

        val purchaseDao = (application as PosApplication).database.purchaseDao()
        purchasesRepository = PurchasesRepository(purchaseDao)

        toolbar = findViewById(R.id.toolbar)
        editTextSupplierName = findViewById(R.id.editTextSupplierName)
        textViewPurchaseDate = findViewById(R.id.textViewPurchaseDate)
        recyclerViewPurchaseItems = findViewById(R.id.recyclerViewPurchaseItems)
        buttonAddItemToPurchase = findViewById(R.id.buttonAddItemToPurchase)
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount)
        buttonSavePurchase = findViewById(R.id.buttonSavePurchase)
        editTextPurchaseNote = findViewById(R.id.editTextPurchaseNote)

        toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()

        editingPurchaseId = intent.getIntExtra("EDIT_PURCHASE_ID", -1)
        if (editingPurchaseId != -1) {
            toolbar.title = "အဝယ်မှတ်တမ်း ပြင်ဆင်ရန်"
            loadPurchaseData()
        } else {
            toolbar.title = "အဝယ်စာရင်းသစ်ထည့်ရန်"
            updateDateInView()
        }

        updateTotalAmount()

        textViewPurchaseDate.setOnClickListener { showDatePickerDialog() }
        buttonAddItemToPurchase.setOnClickListener { showAddItemDialog() }
        buttonSavePurchase.setOnClickListener { savePurchase() }
    }

    private fun loadPurchaseData() {
        lifecycleScope.launch {
            val purchaseItem = purchasesRepository.getPurchaseById(editingPurchaseId)
            if (purchaseItem != null) {
                runOnUiThread {
                    editTextSupplierName.setText(purchaseItem.supplierName)
                    textViewPurchaseDate.text = purchaseItem.purchaseDate
                    editTextPurchaseNote.setText(purchaseItem.note)

                    purchaseDetailItems.clear()
                    // Null check ထည့်ပြီးမှ addAll လုပ်ပါ
                    purchaseItem.items?.let {
                        purchaseDetailItems.addAll(it)
                    }
                    purchaseDetailAdapter.notifyDataSetChanged()
                    updateTotalAmount()

                    val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                    calendar.time = sdf.parse(purchaseItem.purchaseDate) ?: Date()
                }
            }
        }
    }

    private fun savePurchase() {
        val supplierName = editTextSupplierName.text.toString()
        val purchaseDate = textViewPurchaseDate.text.toString()
        val note = editTextPurchaseNote.text.toString()

        if (supplierName.isEmpty() || purchaseDetailItems.isEmpty()) {
            Toast.makeText(this, "Supplier အမည်နှင့် အနည်းဆုံး ပစ္စည်းတစ်မျိုး ထည့်ပါ", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAmount = purchaseDetailItems.sumOf { it.quantity * it.purchasePrice }

        lifecycleScope.launch {
            if (editingPurchaseId != -1) {
                val purchaseToUpdate = purchasesRepository.getPurchaseById(editingPurchaseId)
                val updatedPurchase = PurchaseItem(
                    id = editingPurchaseId,
                    supplierName = supplierName,
                    purchaseDate = purchaseDate,
                    items = purchaseDetailItems.toList(),
                    totalAmount = totalAmount,
                    note = note,
                    hasArrived = purchaseToUpdate?.hasArrived ?: false
                )
                purchasesRepository.updatePurchaseItem(updatedPurchase)
                runOnUiThread {
                    Toast.makeText(this@AddPurchaseActivity, "ပြင်ဆင်ပြီးပါပြီ", Toast.LENGTH_SHORT).show()
                }
            } else {
                val newPurchase = PurchaseItem(
                    supplierName = supplierName,
                    purchaseDate = purchaseDate,
                    items = purchaseDetailItems.toList(),
                    totalAmount = totalAmount,
                    note = note,
                    hasArrived = false
                )
                purchasesRepository.addPurchaseItem(newPurchase)
                runOnUiThread {
                    Toast.makeText(this@AddPurchaseActivity, "အောင်မြင်စွာ သိမ်းဆည်းပြီးပါပြီ", Toast.LENGTH_SHORT).show()
                }
            }
            finish()
        }
    }

    private fun setupRecyclerView() {
        purchaseDetailAdapter = PurchaseDetailAdapter(purchaseDetailItems, this)
        recyclerViewPurchaseItems.adapter = purchaseDetailAdapter
        recyclerViewPurchaseItems.layoutManager = LinearLayoutManager(this)
    }

    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_purchase_item, null)
        val editTextItemName = dialogView.findViewById<EditText>(R.id.editTextItemName)
        val editTextQuantity = dialogView.findViewById<EditText>(R.id.editTextQuantity)
        val editTextPrice = dialogView.findViewById<EditText>(R.id.editTextPrice)

        AlertDialog.Builder(this)
            .setTitle("ဝယ်ယူသည့် ပစ္စည်းထည့်ရန်")
            .setView(dialogView)
            .setPositiveButton("ထည့်မည်") { dialog, _ ->
                val name = editTextItemName.text.toString()
                val quantityStr = editTextQuantity.text.toString()
                val priceStr = editTextPrice.text.toString()

                if (name.isNotEmpty() && quantityStr.isNotEmpty() && priceStr.isNotEmpty()) {
                    val newItem = PurchaseDetailItem(name, quantityStr.toInt(), priceStr.toDouble())
                    purchaseDetailItems.add(newItem)
                    purchaseDetailAdapter.notifyItemInserted(purchaseDetailItems.size - 1)
                    updateTotalAmount()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "အချက်အလက် အပြည့်အစုံ ဖြည့်ပါ", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("မလုပ်တော့ပါ") { dialog, _ -> dialog.cancel() }
            .create()
            .show()
    }

    private fun updateTotalAmount() {
        val total = purchaseDetailItems.sumOf { it.quantity * it.purchasePrice }
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        textViewTotalAmount.text = "Total: ${numberFormat.format(total.toInt())} Ks"
    }

    override fun onDeleteItem(position: Int) {
        purchaseDetailItems.removeAt(position)
        purchaseDetailAdapter.notifyItemRemoved(position)
        updateTotalAmount()
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
        textViewPurchaseDate.text = sdf.format(calendar.time)
    }
}

