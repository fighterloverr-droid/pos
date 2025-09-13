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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
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

    private lateinit var purchaseDetailAdapter: PurchaseDetailAdapter
    private val purchaseDetailItems = mutableListOf<PurchaseDetailItem>()

    private val calendar = Calendar.getInstance()
    // editingPosition logic is pending for future steps

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_purchase)

        toolbar = findViewById(R.id.toolbar)
        editTextSupplierName = findViewById(R.id.editTextSupplierName)
        textViewPurchaseDate = findViewById(R.id.textViewPurchaseDate)
        recyclerViewPurchaseItems = findViewById(R.id.recyclerViewPurchaseItems)
        buttonAddItemToPurchase = findViewById(R.id.buttonAddItemToPurchase)
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount)
        buttonSavePurchase = findViewById(R.id.buttonSavePurchase)

        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = "အဝယ်စာရင်းသစ်ထည့်ရန်"

        setupRecyclerView()

        updateDateInView()
        updateTotalAmount()

        textViewPurchaseDate.setOnClickListener { showDatePickerDialog() }
        buttonAddItemToPurchase.setOnClickListener { showAddItemDialog() }
        buttonSavePurchase.setOnClickListener { savePurchase() }
    }

    private fun setupRecyclerView() {
        purchaseDetailAdapter = PurchaseDetailAdapter(purchaseDetailItems, this)
        recyclerViewPurchaseItems.adapter = purchaseDetailAdapter
        recyclerViewPurchaseItems.layoutManager = LinearLayoutManager(this)
    }

    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null)
        val editTextItemName = dialogView.findViewById<EditText>(R.id.editTextItemName)
        val editTextQuantity = dialogView.findViewById<EditText>(R.id.editTextQuantity)
        val editTextPrice = dialogView.findViewById<EditText>(R.id.editTextPrice)
        editTextPrice.hint = "တစ်ခုချင်း ဝယ်ဈေး"

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

    private fun savePurchase() {
        val supplierName = editTextSupplierName.text.toString()
        val purchaseDate = textViewPurchaseDate.text.toString()

        if (supplierName.isEmpty()) {
            Toast.makeText(this, "Supplier အမည် ဖြည့်ပါ", Toast.LENGTH_SHORT).show()
            return
        }
        if (purchaseDetailItems.isEmpty()) {
            Toast.makeText(this, "အနည်းဆုံး ပစ္စည်းတစ်မျိုး ထည့်ပါ", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAmount = purchaseDetailItems.sumOf { it.quantity * it.purchasePrice }

        val newPurchase = PurchaseItem(
            supplierName = supplierName,
            purchaseDate = purchaseDate,
            items = purchaseDetailItems.toList(),
            totalAmount = totalAmount
        )

        // 1. အဝယ်မှတ်တမ်းကို Repository ထဲ သိမ်းပါ
        PurchasesRepository.addPurchaseItem(newPurchase)

        // 2. အဲ့ဒီက ပစ္စည်းတွေကို Inventory Stock ထဲကို ပေါင်းထည့်ပါ
        InventoryRepository.addStockFromPurchase(purchaseDetailItems.toList())

        Toast.makeText(this, "အဝယ်စာရင်းကို အောင်မြင်စွာ သိမ်းဆည်းပြီးပါပြီ", Toast.LENGTH_SHORT).show()
        finish()
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